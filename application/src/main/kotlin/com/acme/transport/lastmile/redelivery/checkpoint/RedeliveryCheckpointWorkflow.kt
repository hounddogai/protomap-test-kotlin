package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery checkpoints through their life cycle and keeps a journal of what it did. */
data class RedeliveryCheckpointService(
    var store: RedeliveryCheckpointStore,
    var validator: RedeliveryCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("redelivery checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("redelivery checkpoint $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newRedeliveryCheckpointService(store: RedeliveryCheckpointStore, clock: Clock, flags: Registry): RedeliveryCheckpointService {
    return RedeliveryCheckpointService(
        store = store,
        validator = defaultRedeliveryCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryCheckpointEngine(flags),
    )
}
