package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves scheduled route offset quotas through their life cycle and keeps a journal of what it did. */
data class ScheduledRouteOffsetQuotaWorkflow(
    var store: ScheduledRouteOffsetQuotaStore,
    var validator: ScheduledRouteOffsetQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRouteOffsetQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled route offset quota $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRouteOffsetQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled route offset quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRouteOffsetQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled route offset quota $code not found")
        val result = engine.price(item)
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

fun newScheduledRouteOffsetQuotaWorkflow(store: ScheduledRouteOffsetQuotaStore, clock: Clock, flags: Registry): ScheduledRouteOffsetQuotaWorkflow {
    return ScheduledRouteOffsetQuotaWorkflow(
        store = store,
        validator = defaultScheduledRouteOffsetQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRouteOffsetQuotaEngine(flags),
    )
}
