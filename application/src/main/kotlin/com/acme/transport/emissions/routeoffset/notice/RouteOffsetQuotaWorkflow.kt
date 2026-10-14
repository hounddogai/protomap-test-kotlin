package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves route offset quotas through their life cycle and keeps a journal of what it did. */
data class RouteOffsetQuotaService(
    var store: RouteOffsetQuotaStore,
    var validator: RouteOffsetQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset quota $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("route offset quota $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRouteOffsetQuotaService(store: RouteOffsetQuotaStore, clock: Clock, flags: Registry): RouteOffsetQuotaService {
    return RouteOffsetQuotaService(
        store = store,
        validator = defaultRouteOffsetQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetQuotaEngine(flags),
    )
}
