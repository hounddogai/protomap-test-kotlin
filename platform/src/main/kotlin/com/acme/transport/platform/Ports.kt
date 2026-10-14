package com.acme.transport.platform

import io.grpc.Context
import io.grpc.kotlin.GrpcContextElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/** A domain owns its outbound port; its adapter owns the generated stub. */
fun interface OutboundPort<Q, R> {
    suspend fun execute(request: Q): R
}

class DelegatingPort<Q, R>(delegate: OutboundPort<Q, R>) : OutboundPort<Q, R> by delegate

/** Commands preserve the typed payload while workflows distinguish live and replayed work. */
sealed class Submission<out Q> {
    data class Live<Q>(val payload: Q) : Submission<Q>()
    data class Replay<Q>(val payload: Q, val checkpoint: String) : Submission<Q>()
}

class RequestFactory private constructor() {
    companion object {
        fun <Q> create(build: () -> Q): Q = build()
    }
}

/** The composition root registers clients; adapters with runtime service selection read them by a stable key. */
object ClientRegistry {
    private val clients = ConcurrentHashMap<String, Any>()

    fun register(name: String, client: Any) {
        clients[name] = client
    }

    fun <T : Any> lookup(name: String, type: Class<T>): T =
        type.cast(clients[name] ?: error("No client registered for $name"))
}

/** A process-owned worker consumes a bounded mailbox, outside the publishing RPC's coroutine tree. */
class ReplyMailbox<Q, R>(scope: CoroutineScope, execute: suspend (Q) -> R) {
    private data class Delivery<Q, R>(val request: Q, val answer: CompletableDeferred<R>, val caller: Context)
    private val queue = Channel<Delivery<Q, R>>(64)

    init {
        scope.launch {
            for (delivery in queue) {
                if (delivery.answer.isCancelled) continue
                try {
                    val result = withContext(GrpcContextElement(delivery.caller)) { execute(delivery.request) }
                    delivery.answer.complete(result)
                } catch (error: CancellationException) {
                    delivery.answer.completeExceptionally(error)
                    currentCoroutineContext().ensureActive()
                } catch (error: Exception) {
                    delivery.answer.completeExceptionally(error)
                }
            }
        }.invokeOnCompletion { queue.cancel() }
    }

    suspend fun submit(request: Q): R {
        val answer = CompletableDeferred<R>()
        try {
            queue.send(Delivery(request, answer, Context.current().fork()))
            return answer.await()
        } finally {
            answer.cancel()
        }
    }
}
