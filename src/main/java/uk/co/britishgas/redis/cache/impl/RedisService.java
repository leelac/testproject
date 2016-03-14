package uk.co.britishgas.redis.cache.impl;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import uk.co.britishgas.redis.cache.CacheService;
import uk.co.britishgas.redis.models.Book;
 
@Service("cacheService")
public class RedisService implements CacheService {
	
	@Resource(name = "redisTemplate")
	private RedisTemplate<String, Book> redisTemplate;
 
   /* @Resource(name = "redisTemplate")
    private ListOperations<String, Object> messageList;
 
    @Resource(name = "redisTemplate")
    private RedisOperations<String,Object> latestMessageExpiration;
 
    @Override
    public void addMessage(String user,String message) {
 
        messageList.leftPush(user,message);
 
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        Date date = Date.from(zonedDateTime.plus(1, ChronoUnit.MINUTES).toInstant());
        latestMessageExpiration.expireAt(user,date);
    }
 
    @Override
    public List<Object> listMessages(String user) {
        return messageList.range(user,0,-1);
    }*/
	
	public void addMessage(Book book) {
		redisTemplate.opsForValue().set(book.getId(), book);
	}
 
	public Book listMessages(String key) {
		return redisTemplate.opsForValue().get(key);
	}
 
}
