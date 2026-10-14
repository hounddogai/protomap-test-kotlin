package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy utilization variances through their life cycle and keeps a journal of what it did. */
data class LegacyUtilizationVarianceManager(
    var store: LegacyUtilizationVarianceStore,
    var validator: LegacyUtilizationVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy utilization variance $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyUtilizationVarianceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy utilization variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyUtilizationVarianceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newLegacyUtilizationVarianceManager(store: LegacyUtilizationVarianceStore, clock: Clock): LegacyUtilizationVarianceManager {
    return LegacyUtilizationVarianceManager(
        store = store,
        validator = defaultLegacyUtilizationVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
