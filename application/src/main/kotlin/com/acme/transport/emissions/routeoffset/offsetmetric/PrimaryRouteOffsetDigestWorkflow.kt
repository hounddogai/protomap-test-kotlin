package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.clock.Clock

/** Moves primary route offset digests through their life cycle and keeps a journal of what it did. */
data class PrimaryRouteOffsetDigestService(
    var store: PrimaryRouteOffsetDigestStore,
    var validator: PrimaryRouteOffsetDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryRouteOffsetDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary route offset digest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryRouteOffsetDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary route offset digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryRouteOffsetDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary route offset digest $code not found")
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

fun newPrimaryRouteOffsetDigestService(store: PrimaryRouteOffsetDigestStore, clock: Clock): PrimaryRouteOffsetDigestService {
    return PrimaryRouteOffsetDigestService(
        store = store,
        validator = defaultPrimaryRouteOffsetDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryRouteOffsetDigestEngine(),
    )
}
