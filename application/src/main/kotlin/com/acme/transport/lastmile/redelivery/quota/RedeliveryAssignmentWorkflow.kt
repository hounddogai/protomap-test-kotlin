package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery assignments through their life cycle and keeps a journal of what it did. */
data class RedeliveryAssignmentDesk(
    var store: RedeliveryAssignmentStore,
    var validator: RedeliveryAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("redelivery assignment $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery assignment $code not found")
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

fun newRedeliveryAssignmentDesk(store: RedeliveryAssignmentStore, clock: Clock, flags: Registry): RedeliveryAssignmentDesk {
    return RedeliveryAssignmentDesk(
        store = store,
        validator = defaultRedeliveryAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryAssignmentEngine(flags),
    )
}
