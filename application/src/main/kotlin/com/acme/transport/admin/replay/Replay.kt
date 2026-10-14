package com.acme.transport.admin.replay

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.CallOptions
import io.grpc.ManagedChannelBuilder
import io.grpc.MethodDescriptor
import io.grpc.stub.ClientCalls
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/** A wire-level support tool replays an operator-selected unary RPC and stores its binary reply. */
private object WireBytes : MethodDescriptor.Marshaller<ByteArray> {
    override fun stream(value: ByteArray): InputStream = ByteArrayInputStream(value)
    override fun parse(stream: InputStream): ByteArray = stream.readBytes()
}

fun main(args: Array<String>) {
    require(args.size == 3) { "Usage: replay package.Service/Method request.pb response.pb" }
    require('/' in args[0] && !args[0].startsWith('/')) { "Use the canonical service/method path" }
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"])
    val token = "Bearer " + requireNotNull(env["ACCESS_TOKEN"])
    val subject = requireNotNull(env["USER_ID"])
    val channel = ManagedChannelBuilder.forTarget(Settings.fromEnvironment(env).meshTarget).usePlaintext()
        .intercept(CallerHeaders(token, tenant, subject)).build()
    try {
        val descriptor = MethodDescriptor.newBuilder<ByteArray, ByteArray>()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(args[0])
            .setRequestMarshaller(WireBytes)
            .setResponseMarshaller(WireBytes)
            .build()
        val payload = Files.readAllBytes(Path.of(args[1]))
        val reply = ClientCalls.blockingUnaryCall(
            channel, descriptor, CallOptions.DEFAULT.withDeadlineAfter(10, TimeUnit.SECONDS), payload,
        )
        Files.write(Path.of(args[2]), reply)
    } finally {
        channel.shutdownNow()
    }
}
