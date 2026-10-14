package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback service plan rollups through their life cycle and keeps a journal of what it did. */
data class FallbackServicePlanRollupWorkflow(
    var store: FallbackServicePlanRollupStore,
    var validator: FallbackServicePlanRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackServicePlanRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback service plan rollup $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackServicePlanRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback service plan rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackServicePlanRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback service plan rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newFallbackServicePlanRollupWorkflow(store: FallbackServicePlanRollupStore, clock: Clock, flags: Registry): FallbackServicePlanRollupWorkflow {
    return FallbackServicePlanRollupWorkflow(
        store = store,
        validator = defaultFallbackServicePlanRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackServicePlanRollupEngine(flags),
    )
}
