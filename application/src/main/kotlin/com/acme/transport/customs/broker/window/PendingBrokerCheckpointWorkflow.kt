package com.acme.transport.customs.broker.window

import com.acme.transport.customs.clock.Clock

/** Moves pending broker checkpoints through their life cycle and keeps a journal of what it did. */
data class PendingBrokerCheckpointWorkflow(
    var store: PendingBrokerCheckpointStore,
    var validator: PendingBrokerCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingBrokerCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending broker checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(PendingBrokerCheckpointStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending broker checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingBrokerCheckpointStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending broker checkpoint $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newPendingBrokerCheckpointWorkflow(store: PendingBrokerCheckpointStore, clock: Clock): PendingBrokerCheckpointWorkflow {
    return PendingBrokerCheckpointWorkflow(
        store = store,
        validator = defaultPendingBrokerCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingBrokerCheckpointEngine(),
    )
}
