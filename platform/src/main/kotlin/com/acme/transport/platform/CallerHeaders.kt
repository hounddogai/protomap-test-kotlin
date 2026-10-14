package com.acme.transport.platform

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor

/** Forward caller headers through the mesh, or use the credentials supplied to an operator job. */
class CallerHeaders(
    private val token: String = "",
    private val tenant: String = "",
    private val subject: String = "",
) : ClientInterceptor {
    override fun <Q : Any, R : Any> interceptCall(
        method: MethodDescriptor<Q, R>, options: CallOptions, next: Channel,
    ): ClientCall<Q, R> = object : ForwardingClientCall.SimpleForwardingClientCall<Q, R>(next.newCall(method, options)) {
        override fun start(listener: Listener<R>, headers: Metadata) {
            headers.put(CallerIdentity.authorization, CallerIdentity.token.get() ?: token)
            headers.put(CallerIdentity.tenantHeader, CallerIdentity.tenant.get() ?: tenant)
            headers.put(CallerIdentity.subjectHeader, CallerIdentity.subject.get() ?: subject)
            super.start(listener, headers)
        }
    }
}
