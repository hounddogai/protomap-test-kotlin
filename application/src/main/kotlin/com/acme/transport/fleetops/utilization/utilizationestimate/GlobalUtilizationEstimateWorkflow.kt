package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global utilization estimates through their life cycle and keeps a journal of what it did. */
data class GlobalUtilizationEstimateWorkflow(
    var store: GlobalUtilizationEstimateStore,
    var validator: GlobalUtilizationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalUtilizationEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global utilization estimate $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalUtilizationEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global utilization estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalUtilizationEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global utilization estimate $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newGlobalUtilizationEstimateWorkflow(store: GlobalUtilizationEstimateStore, clock: Clock, flags: Registry): GlobalUtilizationEstimateWorkflow {
    return GlobalUtilizationEstimateWorkflow(
        store = store,
        validator = defaultGlobalUtilizationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalUtilizationEstimateEngine(flags),
    )
}
