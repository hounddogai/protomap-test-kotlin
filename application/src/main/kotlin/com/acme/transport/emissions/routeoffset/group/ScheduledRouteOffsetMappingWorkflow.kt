package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves scheduled route offset mappings through their life cycle and keeps a journal of what it did.
 */
data class ScheduledRouteOffsetMappingDesk(
    var store: ScheduledRouteOffsetMappingStore,
    var validator: ScheduledRouteOffsetMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRouteOffsetMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled route offset mapping $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRouteOffsetMappingStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled route offset mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRouteOffsetMappingStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled route offset mapping $code not found")
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

fun newScheduledRouteOffsetMappingDesk(store: ScheduledRouteOffsetMappingStore, clock: Clock, flags: Registry): ScheduledRouteOffsetMappingDesk {
    return ScheduledRouteOffsetMappingDesk(
        store = store,
        validator = defaultScheduledRouteOffsetMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRouteOffsetMappingEngine(flags),
    )
}
