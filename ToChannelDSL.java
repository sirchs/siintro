package com.kris.intro.integration;


import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class ToChannelDSL {

    private static final String INPUT_CHANNEL_NAME  = "InputChannel_DSL";
    private static final String OUTPUT_CHANNEL_NAME = "OutputChannel_DSL";

    String appname;

    @MessagingGateway(name = "Hello world DSL")
    interface InputGateway {
        @Gateway(requestChannel = INPUT_CHANNEL_NAME)
        void upload(String data);
//        String upload(String data);
    }

//    @Bean(name = INPUT_CHANNEL_NAME)
//    MessageChannel inputChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean(name = OUTPUT_CHANNEL_NAME)
//    MessageChannel outputChannel() {
//        return new DirectChannel();
//    }

//    @Component
//    static class  DataMessageProvider {
//        @ServiceActivator(inputChannel = INPUT_CHANNEL_NAME, outputChannel=OUTPUT_CHANNEL_NAME)
//        public String processData(String data) {
//            System.out.print("\n---> \n Input channel input data: " + data + "\n---->\n");
//            return "Input chanel output data:  [" + data + "]";
//        }
//    }
//
//    @Component
//    static class  DataMessageResponse {
//        @ServiceActivator(inputChannel = OUTPUT_CHANNEL_NAME/*, outputChannel=OUTPUT_CHANNEL_NAME*/)
//        public void processData(String data) {
//            System.out.print("\n---> \n Output channel input data: " + data + "\n---->\n");
////            return "Output channel output data";
//        }
//    }

    @Bean()
    public IntegrationFlow inputDataFlow() {

        return IntegrationFlows
//                .from(INPUT_CHANNEL_NAME)
                .from(MessageChannels.direct(INPUT_CHANNEL_NAME))
//                .from(Http.inboundGateway("/hello")
//                        .requestMapping(m -> m.methods(HttpMethod.POST))
//                        .requestPayloadType(String.class)
//                )
//                .enrichHeaders(customErrorChannelHeader())
//                .channel(MessageChannels.direct(INPUT_CHANNEL_NAME))
                .wireTap(f -> f.handle((MessageHandler) message -> {
                    System.out.println("Wire tap Message:  " + message);
                }))
//                .handle(setLoggingPropertiesHandler())
                .handle(dummyMessageHandler())
//                .handle(responseSuccessHandler(getConfigFrom().getLocalArchive(), true))
//                .handle(clearLoggingPropertiesHandler())

//                .transform(":  "::concat)
//                .transform(appname::concat)
//                .transform("Next custom std output handler: "::concat)

//                .<String, String>transform(String::toUpperCase) //tak lub jak nizej
                .transform(String.class, String::toUpperCase)
                .channel(/*MessageChannels.publishSubscribe*/("outputChannell"))
//                .handle(System.out::println)
//                .handle(CharacterStreamWritingMessageHandler.stdout())
               .get();
    }

    @Bean("outputChannell")
    MessageChannel outputChannel() {
        return new DirectChannel();  //PublishSubscribeChannel();
    }

    @Bean()
    public IntegrationFlow outputDataFlow() {

        return IntegrationFlows
//                .from(INPUT_CHANNEL_NAME)
                .from(/*MessageChannels.direct*/("outputChannell"))
//                .enrichHeaders(customErrorChannelHeader())
//                .channel(MessageChannels.direct(INPUT_CHANNEL_NAME))
                .wireTap(f -> f.handle(message -> {
                    System.out.println("Wire tap Output data flow, Message:  " + message);
                }))
                .handle(dummyMessageHandler())
                //.handle(Http.outboundChannelAdapter("http://localhost:8081//out"))
                //.handle(new FileWritingMessageHandler(new File(".\\target\\logs")))
//                .log()
//                .transform(":  "::concat)
//                .transform(appname::concat)
//                .transform("Next custom std output handler: "::concat)
//                .channel("output")
//                .handle(System.out::println)
                //.channel("subOutput")
//                .publishSubscribeChannel(sc -> sc
//                    .subscribe(sf->sf
//                            .transform("subChannel 1 "::concat)
////                            .handle(System.out::println)
//                            .handle(Ftp
//                                    .outboundAdapter(getSessionFactory())
//                                    .autoCreateDirectory(true)
//                                    .remoteDirectory("/"),
//                                c -> c.advice(new AbstractRequestHandlerAdvice() {
//                                    @Override
//                                    protected Object doInvoke(ExecutionCallback callback, Object target, Message<?> message) throws Exception {
//                                        Object ret = null;
//                                        try {
//                                            ret = callback.execute();
//                                        }  catch (Exception e) {
//                                            System.out.println("Exception handling for: , "+ message);
//                                            System.out.println("exception message: " + e.getMessage());
//                                            System.out.println("Return: "+ret);
//                                        }
//                                        return message;
//                                    }
//                                })))
//
////                        .handle(System.out::println))
//                    .subscribe(sf -> sf
//                            .transform("subChannel 2 "::concat)
//                            .handle(System.out::println)))
                .handle(
                        Ftp
                                .outboundAdapter(getSessionFactory())
                                .autoCreateDirectory(true)
                                .useTemporaryFileName(false)
                                .remoteDirectory("\\")  //  This is the end of the integration flow.
                        ,
                        c -> c.advice(new AbstractRequestHandlerAdvice() {
                                    @Override
                                    protected Object doInvoke(ExecutionCallback callback, Object target, Message<?> message) throws Exception {
                                        try {
                                            callback.execute();
                                            //here I can send message forward to the next channel
                                            //destinationChannel.send(message)

                                            System.out.println("After FTP message ");
                                        }  catch (Exception e) {
                                            System.out.println("Exception handling for: , "+ message);
                                            System.out.println("exception message: " + e.getMessage());

                                            //here I can send message forward to the next channel - particulare error channel
                                            //
                                            //errorChannel.send(new GenericMessage(new MessagingException(message, unwrapExceptionIfNecessary(e))));
                                        }
                                        return message;
                                    }
                                })
                )
//                .transform("post ftp "::concat)
//                .handle(System.out::println)
                .get();
    }

    SessionFactory<FTPFile> getSessionFactory() {
        DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
        factory.setHost("localhost");
//        factory.setPort(getPort());
        factory.setUsername("user");
        factory.setPassword("ps");
//        factory.setTimeout(getTimeout());

        return factory;
    }

    private MessageHandler dummyMessageHandler() {

        return new AbstractReplyProducingMessageHandler() {
            @Override
            protected Object handleRequestMessage(final Message<?> message) {
                System.out.println("Message:  " + message);
                //if You do need to continue any flow just return null otherwise return something you want to move forward
                //to the next handler ... again if you have next handlers otherwise you end up with exception that
                // Dispatcher has no subscribers for channel 'unknown.channel.name'
                return message;
                //return message;
            }
        };
    }
}
