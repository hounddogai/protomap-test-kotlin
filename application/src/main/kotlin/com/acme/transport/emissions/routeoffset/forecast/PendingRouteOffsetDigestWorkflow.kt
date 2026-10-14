package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending route offset digests through their life cycle and keeps a journal of what it did. */
data class PendingRouteOffsetDigestManager(
    var store: PendingRouteOffsetDigestStore,
    var validator: PendingRouteOffsetDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRouteOffsetDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending route offset digest $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRouteOffsetDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending route offset digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRouteOffsetDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending route offset digest $code not found")
        val result = engine.evaluate(item)
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

fun newPendingRouteOffsetDigestManager(store: PendingRouteOffsetDigestStore, clock: Clock, flags: Registry): PendingRouteOffsetDigestManager {
    return PendingRouteOffsetDigestManager(
        store = store,
        validator = defaultPendingRouteOffsetDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRouteOffsetDigestEngine(flags),
    )
}
