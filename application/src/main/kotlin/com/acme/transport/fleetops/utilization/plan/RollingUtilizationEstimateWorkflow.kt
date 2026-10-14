package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling utilization estimates through their life cycle and keeps a journal of what it did. */
data class RollingUtilizationEstimateDesk(
    var store: RollingUtilizationEstimateStore,
    var validator: RollingUtilizationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingUtilizationEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling utilization estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RollingUtilizationEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling utilization estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingUtilizationEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling utilization estimate $code not found")
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

fun newRollingUtilizationEstimateDesk(store: RollingUtilizationEstimateStore, clock: Clock, flags: Registry): RollingUtilizationEstimateDesk {
    return RollingUtilizationEstimateDesk(
        store = store,
        validator = defaultRollingUtilizationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingUtilizationEstimateEngine(flags),
    )
}
