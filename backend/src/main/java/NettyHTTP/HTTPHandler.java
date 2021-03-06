package NettyHTTP;

import RabbitMQ.Sender;
import RabbitMQ.RabbitMQConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.buffer.Unpooled;

import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class HTTPHandler extends SimpleChannelInboundHandler<Object> {
    private HttpRequest request;
    private String requestBody;
    private String correlationId = null;
    volatile String responseBody;
    Logger log = Logger.getLogger(NettyHTTPServer.class.getName());
    ExecutorService executorService = Executors.newCachedThreadPool();
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (correlationId == null)
            correlationId = UUID.randomUUID().toString();
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            setRequestBody(content.toString(CharsetUtil.UTF_8));
            ctx.fireChannelRead(content.copy());
        }
        if (msg instanceof LastHttpContent) {
            writeResponse(ctx);
        }
    }

    private synchronized void writeResponse(final ChannelHandlerContext ctx) throws ExecutionException, InterruptedException {

        JSONObject requestJson = new JSONObject(requestBody);

        Notifier notifier = new Notifier(this, requestJson.getString("app"));
        

        sendToMQ(requestBody, requestJson.getString("app").toUpperCase());
        Future future = executorService.submit(notifier);
        this.responseBody = (String) future.get();

        setResponseBody(this.responseBody);

        if (this.responseBody == null) {
            System.out.println("Null Response Method: " + requestJson.getString("method"));
        }



        JSONObject json = new JSONObject(getResponseBody());
        HttpResponseStatus status = null;

        if (!json.has("message"))
            status = new HttpResponseStatus(Integer.parseInt((String) json
                    .get("code")),
                    Integer.parseInt((String) json.get("code")) == 200 ? "Ok"
                            : "Bad Request");
        else
            status = new HttpResponseStatus(Integer.parseInt((String) json
                    .get("code")), (String) json.get("message"));

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        if (keepAlive) {
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ctx.writeAndFlush(response);

    }

    private void sendToMQ(String message, String queue) {
        Sender s = new Sender(new RabbitMQConfig(queue+".INQUEUE"));
        s.send(message, correlationId, log);
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public synchronized void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getCorrelationId() {
        return correlationId;
    }

}

