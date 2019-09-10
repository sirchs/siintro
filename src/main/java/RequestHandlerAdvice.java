package com.kris.intro.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import java.io.File;
import java.util.Objects;
@Slf4j
class RequestHandlerAdvice extends AbstractRequestHandlerAdvice {

    private final MessageHandler onSuccessHandler;
    private final MessageChannel failureChannel;


    protected RequestHandlerAdvice(MessageHandler successHandler, final MessageChannel errorChannel) {
        this.onSuccessHandler = successHandler;
        this.failureChannel = errorChannel;
        }

    @Override
    protected Object doInvoke(AbstractRequestHandlerAdvice.ExecutionCallback callback, Object target, Message<?> message) {
        try {
            Object payload = message.getPayload();
            if (payload instanceof File) {
                File toMain = (File)payload;
                if (toMain.length() == 0) {
                    toMain.createNewFile();  //force to physically create a file, even it is empty
                    throw new IllegalStateException("File is empty, nothing to process." );
                }
            }

            Object result = callback.execute();
            if (Objects.nonNull(onSuccessHandler)) {
                onSuccessHandler.handleMessage(message);
            }
            return result;
        } catch (Exception e) {
            Exception actualException = unwrapExceptionIfNecessary(e);
            log.error(actualException.getMessage());
            if (Objects.nonNull(failureChannel)) {
                Message<MessagingException> messagingException = new GenericMessage(new MessagingException(message, actualException));
                failureChannel.send(messagingException);
            }
            return null;
        }
    }
}
