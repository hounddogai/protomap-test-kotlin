package com.acme.transport.platform

import com.acme.common.v1.actor
import com.acme.common.v1.requestContext
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.ValidateUserRequestKt
import com.acme.identity.v1.user
import com.acme.identity.v1.validateUserRequest
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

/** Request credentials are carried by gRPC Context into coroutine handlers and outbound interceptors. */
object CallerIdentity {
    val token: Context.Key<String> = Context.key("acme-token")
    val tenant: Context.Key<String> = Context.key("acme-tenant")
    val subject: Context.Key<String> = Context.key("acme-subject")
    val authorization: Metadata.Key<String> = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    val tenantHeader: Metadata.Key<String> = Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER)
    val subjectHeader: Metadata.Key<String> = Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER)
}

/** Authenticate every RPC on the server's worker executor before allowing it to reach a domain. */
class IdentityInterceptor(
    private val identity: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub,
    private val serviceToken: String,
) : ServerInterceptor {
    override fun <Q : Any, R : Any> interceptCall(
        call: ServerCall<Q, R>, headers: Metadata, next: ServerCallHandler<Q, R>,
    ): ServerCall.Listener<Q> {
        val authorization = headers.get(CallerIdentity.authorization).orEmpty()
        val tenant = headers.get(CallerIdentity.tenantHeader).orEmpty()
        val token = authorization.removePrefix("Bearer ")
        if (!authorization.startsWith("Bearer ") || token.isEmpty() || tenant.isBlank()) {
            call.close(Status.UNAUTHENTICATED.withDescription("bearer token and tenant are required"), Metadata())
            return object : ServerCall.Listener<Q>() {}
        }
        // The lookup is the server's own call, which bears its workload credential.
        val workload = Metadata().apply {
            put(CallerIdentity.authorization, "Bearer $serviceToken")
            put(CallerIdentity.tenantHeader, tenant)
        }
        val subject = try {
            val response = runBlocking {
                identity.withDeadlineAfter(3, TimeUnit.SECONDS).validateUser(validateUserRequest {
                    this.context = requestContext {
                        tenantId = tenant
                        this.actor = actor { userId = "transport"; serviceAccount = true }
                    }
                    userId = token
                    this.user = user { attributes["bearer_token"] = token }
                    this.details = ValidateUserRequestKt.details { strict = true; rules.add("authenticate_bearer") }
                }, workload)
            }
            val resolved = response.user.userId
            if (response.result.status != "ok" || resolved != token) {
                call.close(Status.PERMISSION_DENIED.withDescription("identity policy rejected the caller"), Metadata())
                return object : ServerCall.Listener<Q>() {}
            }
            resolved
        } catch (error: Exception) {
            call.close(Status.UNAUTHENTICATED.withDescription("identity check failed").withCause(error), Metadata())
            return object : ServerCall.Listener<Q>() {}
        }
        val context = Context.current().withValues(
            CallerIdentity.token, authorization, CallerIdentity.tenant, tenant, CallerIdentity.subject, subject,
        )
        return Contexts.interceptCall(context, call, headers, next)
    }
}
