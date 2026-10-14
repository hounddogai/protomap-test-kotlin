package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves regional utilization overrides through their life cycle and keeps a journal of what it did.
 */
data class RegionalUtilizationOverrideCoordinator(
    var store: RegionalUtilizationOverrideStore,
    var validator: RegionalUtilizationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional utilization override $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalUtilizationOverrideStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional utilization override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalUtilizationOverrideStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRegionalUtilizationOverrideCoordinator(store: RegionalUtilizationOverrideStore, clock: Clock): RegionalUtilizationOverrideCoordinator {
    return RegionalUtilizationOverrideCoordinator(
        store = store,
        validator = defaultRegionalUtilizationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
