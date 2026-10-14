package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy inspection rules through their life cycle and keeps a journal of what it did. */
data class LegacyInspectionRuleCoordinator(
    var store: LegacyInspectionRuleStore,
    var validator: LegacyInspectionRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyInspectionRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy inspection rule $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyInspectionRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy inspection rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyInspectionRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy inspection rule $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newLegacyInspectionRuleCoordinator(store: LegacyInspectionRuleStore, clock: Clock, flags: Registry): LegacyInspectionRuleCoordinator {
    return LegacyInspectionRuleCoordinator(
        store = store,
        validator = defaultLegacyInspectionRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyInspectionRuleEngine(flags),
    )
}
