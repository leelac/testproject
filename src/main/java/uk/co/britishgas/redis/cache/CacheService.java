package uk.co.britishgas.redis.cache;

import uk.co.britishgas.redis.models.Book;
 
public interface CacheService {
 
    public void addMessage(Book book);
 
    public Book listMessages(String user);
 
}