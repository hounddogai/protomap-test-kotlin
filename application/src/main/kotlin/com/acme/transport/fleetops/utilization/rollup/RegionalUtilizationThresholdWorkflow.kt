package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves regional utilization thresholds through their life cycle and keeps a journal of what it did.
 */
data class RegionalUtilizationThresholdManager(
    var store: RegionalUtilizationThresholdStore,
    var validator: RegionalUtilizationThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional utilization threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalUtilizationThresholdStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional utilization threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalUtilizationThresholdStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRegionalUtilizationThresholdManager(store: RegionalUtilizationThresholdStore, clock: Clock): RegionalUtilizationThresholdManager {
    return RegionalUtilizationThresholdManager(
        store = store,
        validator = defaultRegionalUtilizationThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
