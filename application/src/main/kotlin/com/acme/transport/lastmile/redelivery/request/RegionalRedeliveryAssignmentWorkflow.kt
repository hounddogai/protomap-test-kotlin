package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves regional redelivery assignments through their life cycle and keeps a journal of what it did.
 */
data class RegionalRedeliveryAssignmentCoordinator(
    var store: RegionalRedeliveryAssignmentStore,
    var validator: RegionalRedeliveryAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalRedeliveryAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional redelivery assignment $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRedeliveryAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional redelivery assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRedeliveryAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional redelivery assignment $code not found")
        val result = engine.price(item)
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

fun newRegionalRedeliveryAssignmentCoordinator(store: RegionalRedeliveryAssignmentStore, clock: Clock, flags: Registry): RegionalRedeliveryAssignmentCoordinator {
    return RegionalRedeliveryAssignmentCoordinator(
        store = store,
        validator = defaultRegionalRedeliveryAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalRedeliveryAssignmentEngine(flags),
    )
}
