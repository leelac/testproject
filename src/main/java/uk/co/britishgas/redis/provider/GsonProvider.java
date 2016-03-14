package uk.co.britishgas.redis.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.google.gson.JsonSyntaxException;

import uk.co.britishgas.redis.annotation.JsonParam;

/**
 * Acts as the data writer & data reader for converting objects to JSON (and
 * vice versa) to be captured as response. Note: Primitives are not valid
 * objects to be converted to JSON. Hence they are omitted. There are no
 * restrictions while reading message content though.
 * 
 * @author govindp1 (Pradeep Krishna Govindaraju)
 *
 */
@Consumes(MediaType.APPLICATION_JSON)
public class GsonProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

	private static final int READ_LIMIT = 4096;

	private Gson gson;
	private Set<Class<?>> primitiveClasses;
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Default constructor to instantiate GsonProvider. It also instantiates
	 * Gson object and primitive classes.
	 */
	public GsonProvider() {
		gson = new GsonBuilder().serializeNulls().create();
		primitiveClasses = new HashSet<Class<?>>();
		primitiveClasses.add(Integer.class);
		primitiveClasses.add(String.class);
		primitiveClasses.add(Byte.class);
		primitiveClasses.add(Long.class);
		primitiveClasses.add(Float.class);
		primitiveClasses.add(Double.class);
		primitiveClasses.add(Boolean.class);
		primitiveClasses.add(Short.class);
		primitiveClasses.add(Character.class);
		primitiveClasses.add(Void.class);
		primitiveClasses.add(Number.class);
		primitiveClasses.add(Enum.class);
	}

	@Override
	public long getSize(Object object, Class<?> className, Type type, Annotation[] annotations, MediaType mediaType) {
		return gson.toJson(object, type).getBytes().length;
	}

	@Override
	public boolean isWriteable(Class<?> className, Type type, Annotation[] annotations, MediaType mediaType) {
		return !className.isPrimitive() && !primitiveClasses.contains(className);
	}

	@Override
	public void writeTo(Object object, Class<?> className, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output) throws IOException {
		if ("application/pdf".equalsIgnoreCase(mediaType.toString())) {
			output.write((byte[]) object);
		} else {
			output.write(gson.toJson(object, type).getBytes());
		}
	}

	@Override
	public boolean isReadable(Class<?> className, Type type, Annotation[] annotations, MediaType mediaType) {
		return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
	}

	@Override
	public Object readFrom(Class<Object> className, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> headers, InputStream inputStream) throws IOException {
		Object reconstructedObject = null;
		JsonParam jsonParam = retrieveJsonParamIfAny(annotations);
		try {
			if (jsonParam == null) {
				reconstructedObject = gson.fromJson(new InputStreamReader(inputStream), className);
			} else {
				reconstructedObject = createTargetInstance(inputStream, className, jsonParam);
			}
		} catch (Exception e) {
			logger.error(
					String.format("Error creating instance of [%s]! Incompatible JSON supplied.", className.toString()),
					e);
			throw new ClientErrorException(Status.BAD_REQUEST, e);
		}
		return reconstructedObject;
	}

	private JsonParam retrieveJsonParamIfAny(Annotation[] annotations) {
		JsonParam jsonParam = null;
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().getName().equals(JsonParam.class.getName())) {
				jsonParam = (JsonParam) annotation;
				break;
			}
		}
		return jsonParam;
	}

	private Object createTargetInstance(InputStream inputStream, Class<Object> className, JsonParam jsonParam) {
		// Note: It is quite important to reset the input stream as it will be
		// reused for multiple JsonParam. Lets mark the input stream for a reset
		// and then reset it once everything is over.
		setupMark(inputStream);
		Object reconstructedObject = null;
		JsonStreamParser parser = new JsonStreamParser(new InputStreamReader(inputStream));
		while (parser.hasNext()) {
			JsonElement element = parser.next();
			if (element.isJsonObject()) {
				for (Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
					if (StringUtils.equals(entry.getKey(), jsonParam.value())) {
						if (jsonParam.isCollection()) {
							reconstructedObject = gson.fromJson(entry.getValue(), className);
							reconstructedObject = createCollection(jsonParam, reconstructedObject);
						} else {
							reconstructedObject = gson.fromJson(entry.getValue(), className);
						}
						//resetStream(inputStream);
						break;
					}
				}
				// We support only one JSON Object
				break;
			} else {
				logger.error(
						String.format("Error creating instance of [%s]! Expected an Object.", className.toString()));
				throw new JsonSyntaxException("Expected an Object!");
			}
		}
		return reconstructedObject;
	}

	@SuppressWarnings("unchecked")
	private Object createCollection(JsonParam jsonParam, Object constructedObject) {
		List<Object> constructedList = new ArrayList<Object>();
		List<Map<String, Object>> dataMaps = (List<Map<String, Object>>) constructedObject;
		BeanUtilsBean bean = new BeanUtilsBean();
		for (Map<String, Object> dataMap : dataMaps) {
			try {
				Object dataObject = jsonParam.type().newInstance();
				bean.populate(dataObject, dataMap);
				constructedList.add(dataObject);
			} catch (Exception exception) {
				logger.error("Error constructing collection", exception);
			}
		}
		return constructedList;
	}

	private void resetStream(InputStream inputStream) {
		try {
			inputStream.reset();
		} catch (IOException e) {
			logger.error("Error reseting input stream", e);
		}
	}

	private void setupMark(InputStream inputStream) {
		logger.error("Is marking supported? {}", inputStream.markSupported());
		try {
			inputStream.mark(READ_LIMIT);
		} catch (Exception e) {
			logger.error("Unable to mark the stream", e);
		}
	}
}