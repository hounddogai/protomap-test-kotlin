package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves capped service area assignments through their life cycle and keeps a journal of what it did.
 */
data class CappedServiceAreaAssignmentWorkflow(
    var store: CappedServiceAreaAssignmentStore,
    var validator: CappedServiceAreaAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedServiceAreaAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped service area assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CappedServiceAreaAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped service area assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedServiceAreaAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped service area assignment $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newCappedServiceAreaAssignmentWorkflow(store: CappedServiceAreaAssignmentStore, clock: Clock, flags: Registry): CappedServiceAreaAssignmentWorkflow {
    return CappedServiceAreaAssignmentWorkflow(
        store = store,
        validator = defaultCappedServiceAreaAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedServiceAreaAssignmentEngine(flags),
    )
}
