package io.sockit.sockitserver;

/**
 * This interface represents a factory for creating an Session Listeners. 
 */
public interface SessionListenerFactory {
    /**
     * Factory method to get a SessionListener for a new session to handle the session events of a client session.
     * @return SessionListener - a SessionListener instance 
     */
    SessionListener getSessionListener();
    
}
