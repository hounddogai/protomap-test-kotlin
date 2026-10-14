package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves lenient utilization assignments through their life cycle and keeps a journal of what it did.
 */
data class LenientUtilizationAssignmentCoordinator(
    var store: LenientUtilizationAssignmentStore,
    var validator: LenientUtilizationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientUtilizationAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient utilization assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientUtilizationAssignmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient utilization assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientUtilizationAssignmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient utilization assignment $code not found")
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

fun newLenientUtilizationAssignmentCoordinator(store: LenientUtilizationAssignmentStore, clock: Clock, flags: Registry): LenientUtilizationAssignmentCoordinator {
    return LenientUtilizationAssignmentCoordinator(
        store = store,
        validator = defaultLenientUtilizationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientUtilizationAssignmentEngine(flags),
    )
}
