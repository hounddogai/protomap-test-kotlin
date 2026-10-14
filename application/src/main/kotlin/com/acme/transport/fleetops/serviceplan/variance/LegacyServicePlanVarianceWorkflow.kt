package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy service plan variances through their life cycle and keeps a journal of what it did. */
data class LegacyServicePlanVarianceDesk(
    var store: LegacyServicePlanVarianceStore,
    var validator: LegacyServicePlanVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy service plan variance $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyServicePlanVarianceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy service plan variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyServicePlanVarianceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newLegacyServicePlanVarianceDesk(store: LegacyServicePlanVarianceStore, clock: Clock): LegacyServicePlanVarianceDesk {
    return LegacyServicePlanVarianceDesk(
        store = store,
        validator = defaultLegacyServicePlanVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
