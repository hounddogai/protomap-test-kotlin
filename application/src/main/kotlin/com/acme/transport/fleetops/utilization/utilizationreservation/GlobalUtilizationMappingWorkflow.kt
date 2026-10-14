package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global utilization mappings through their life cycle and keeps a journal of what it did. */
data class GlobalUtilizationMappingCoordinator(
    var store: GlobalUtilizationMappingStore,
    var validator: GlobalUtilizationMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalUtilizationMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global utilization mapping $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalUtilizationMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global utilization mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalUtilizationMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global utilization mapping $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalUtilizationMappingCoordinator(store: GlobalUtilizationMappingStore, clock: Clock, flags: Registry): GlobalUtilizationMappingCoordinator {
    return GlobalUtilizationMappingCoordinator(
        store = store,
        validator = defaultGlobalUtilizationMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalUtilizationMappingEngine(flags),
    )
}
