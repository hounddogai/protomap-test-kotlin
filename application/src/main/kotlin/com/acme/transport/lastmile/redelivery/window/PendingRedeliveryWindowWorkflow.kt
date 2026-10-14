package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.lastmile.clock.Clock

/** Moves pending redelivery windows through their life cycle and keeps a journal of what it did. */
data class PendingRedeliveryWindowWorkflow(
    var store: PendingRedeliveryWindowStore,
    var validator: PendingRedeliveryWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRedeliveryWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending redelivery window $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRedeliveryWindowStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending redelivery window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRedeliveryWindowStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending redelivery window $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
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

fun newPendingRedeliveryWindowWorkflow(store: PendingRedeliveryWindowStore, clock: Clock): PendingRedeliveryWindowWorkflow {
    return PendingRedeliveryWindowWorkflow(
        store = store,
        validator = defaultPendingRedeliveryWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRedeliveryWindowEngine(),
    )
}
