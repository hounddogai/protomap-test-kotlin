package com.acme.transport.fleetops.inspection.inspectionslot

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection rules through their life cycle and keeps a journal of what it did. */
data class InspectionRuleCoordinator(
    var store: InspectionRuleStore,
    var validator: InspectionRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection rule $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionRuleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionRuleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("inspection rule $code not found")
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

fun newInspectionRuleCoordinator(store: InspectionRuleStore, clock: Clock, flags: Registry): InspectionRuleCoordinator {
    return InspectionRuleCoordinator(
        store = store,
        validator = defaultInspectionRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionRuleEngine(flags),
    )
}
