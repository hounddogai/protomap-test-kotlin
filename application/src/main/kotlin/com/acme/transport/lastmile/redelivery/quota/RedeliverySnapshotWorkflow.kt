package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery snapshots through their life cycle and keeps a journal of what it did. */
data class RedeliverySnapshotWorkflow(
    var store: RedeliverySnapshotStore,
    var validator: RedeliverySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliverySnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("redelivery snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliverySnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliverySnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("redelivery snapshot $code not found")
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

fun newRedeliverySnapshotWorkflow(store: RedeliverySnapshotStore, clock: Clock, flags: Registry): RedeliverySnapshotWorkflow {
    return RedeliverySnapshotWorkflow(
        store = store,
        validator = defaultRedeliverySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliverySnapshotEngine(flags),
    )
}
