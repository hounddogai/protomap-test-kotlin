package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.clock.Clock

/** Moves legacy utilization slots through their life cycle and keeps a journal of what it did. */
data class LegacyUtilizationSlotCoordinator(
    var store: LegacyUtilizationSlotStore,
    var validator: LegacyUtilizationSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyUtilizationSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy utilization slot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyUtilizationSlotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy utilization slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyUtilizationSlotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy utilization slot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLegacyUtilizationSlotCoordinator(store: LegacyUtilizationSlotStore, clock: Clock): LegacyUtilizationSlotCoordinator {
    return LegacyUtilizationSlotCoordinator(
        store = store,
        validator = defaultLegacyUtilizationSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyUtilizationSlotEngine(),
    )
}
