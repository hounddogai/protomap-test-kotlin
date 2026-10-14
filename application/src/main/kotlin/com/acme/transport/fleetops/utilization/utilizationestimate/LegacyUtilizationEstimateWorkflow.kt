package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy utilization estimates through their life cycle and keeps a journal of what it did. */
data class LegacyUtilizationEstimateService(
    var store: LegacyUtilizationEstimateStore,
    var validator: LegacyUtilizationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy utilization estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyUtilizationEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy utilization estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyUtilizationEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newLegacyUtilizationEstimateService(store: LegacyUtilizationEstimateStore, clock: Clock): LegacyUtilizationEstimateService {
    return LegacyUtilizationEstimateService(
        store = store,
        validator = defaultLegacyUtilizationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
