package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.clock.Clock

/**
 * Moves lenient route offset estimates through their life cycle and keeps a journal of what it did.
 */
data class LenientRouteOffsetEstimateCoordinator(
    var store: LenientRouteOffsetEstimateStore,
    var validator: LenientRouteOffsetEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientRouteOffsetEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient route offset estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRouteOffsetEstimateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient route offset estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRouteOffsetEstimateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient route offset estimate $code not found")
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

fun newLenientRouteOffsetEstimateCoordinator(store: LenientRouteOffsetEstimateStore, clock: Clock): LenientRouteOffsetEstimateCoordinator {
    return LenientRouteOffsetEstimateCoordinator(
        store = store,
        validator = defaultLenientRouteOffsetEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientRouteOffsetEstimateEngine(),
    )
}
