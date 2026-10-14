package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.clock.Clock

/** Moves manual availability rules through their life cycle and keeps a journal of what it did. */
data class ManualAvailabilityRuleCoordinator(
    var store: ManualAvailabilityRuleStore,
    var validator: ManualAvailabilityRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualAvailabilityRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual availability rule $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAvailabilityRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual availability rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAvailabilityRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual availability rule $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newManualAvailabilityRuleCoordinator(store: ManualAvailabilityRuleStore, clock: Clock): ManualAvailabilityRuleCoordinator {
    return ManualAvailabilityRuleCoordinator(
        store = store,
        validator = defaultManualAvailabilityRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualAvailabilityRuleEngine(),
    )
}
