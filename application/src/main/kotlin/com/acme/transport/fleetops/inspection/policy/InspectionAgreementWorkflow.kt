package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection agreements through their life cycle and keeps a journal of what it did. */
data class InspectionAgreementCoordinator(
    var store: InspectionAgreementStore,
    var validator: InspectionAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection agreement $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionAgreementStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionAgreementStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("inspection agreement $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newInspectionAgreementCoordinator(store: InspectionAgreementStore, clock: Clock, flags: Registry): InspectionAgreementCoordinator {
    return InspectionAgreementCoordinator(
        store = store,
        validator = defaultInspectionAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionAgreementEngine(flags),
    )
}
