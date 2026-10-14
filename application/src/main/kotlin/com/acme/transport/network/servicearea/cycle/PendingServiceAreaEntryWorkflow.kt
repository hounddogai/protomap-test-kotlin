package com.acme.transport.network.servicearea.cycle

import com.acme.transport.network.clock.Clock

/** Moves pending service area entries through their life cycle and keeps a journal of what it did. */
data class PendingServiceAreaEntryWorkflow(
    var store: PendingServiceAreaEntryStore,
    var validator: PendingServiceAreaEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingServiceAreaEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending service area entry $code not found")
        validator.requireValid(item)
        item.moveTo(PendingServiceAreaEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending service area entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingServiceAreaEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending service area entry $code not found")
        val result = engine.run(item)
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

fun newPendingServiceAreaEntryWorkflow(store: PendingServiceAreaEntryStore, clock: Clock): PendingServiceAreaEntryWorkflow {
    return PendingServiceAreaEntryWorkflow(
        store = store,
        validator = defaultPendingServiceAreaEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingServiceAreaEntryEngine(),
    )
}
