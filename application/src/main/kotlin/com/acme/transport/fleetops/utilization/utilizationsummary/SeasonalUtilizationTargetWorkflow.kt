package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal utilization targets through their life cycle and keeps a journal of what it did. */
data class SeasonalUtilizationTargetManager(
    var store: SeasonalUtilizationTargetStore,
    var validator: SeasonalUtilizationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalUtilizationTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal utilization target $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalUtilizationTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal utilization target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalUtilizationTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("seasonal utilization target $code not found")
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

fun newSeasonalUtilizationTargetManager(store: SeasonalUtilizationTargetStore, clock: Clock, flags: Registry): SeasonalUtilizationTargetManager {
    return SeasonalUtilizationTargetManager(
        store = store,
        validator = defaultSeasonalUtilizationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalUtilizationTargetEngine(flags),
    )
}
