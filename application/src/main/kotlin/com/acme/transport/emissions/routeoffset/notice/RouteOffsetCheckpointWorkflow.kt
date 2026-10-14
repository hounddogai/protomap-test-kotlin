package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves route offset checkpoints through their life cycle and keeps a journal of what it did. */
data class RouteOffsetCheckpointDesk(
    var store: RouteOffsetCheckpointStore,
    var validator: RouteOffsetCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RouteOffsetCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("route offset checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RouteOffsetCheckpointStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("route offset checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RouteOffsetCheckpointStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("route offset checkpoint $code not found")
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

fun newRouteOffsetCheckpointDesk(store: RouteOffsetCheckpointStore, clock: Clock, flags: Registry): RouteOffsetCheckpointDesk {
    return RouteOffsetCheckpointDesk(
        store = store,
        validator = defaultRouteOffsetCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRouteOffsetCheckpointEngine(flags),
    )
}
