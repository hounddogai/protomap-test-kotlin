package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.emissions.clock.Clock

/** Moves strict route offset segments through their life cycle and keeps a journal of what it did. */
data class StrictRouteOffsetSegmentManager(
    var store: StrictRouteOffsetSegmentStore,
    var validator: StrictRouteOffsetSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictRouteOffsetSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict route offset segment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictRouteOffsetSegmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict route offset segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictRouteOffsetSegmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict route offset segment $code not found")
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

fun newStrictRouteOffsetSegmentManager(store: StrictRouteOffsetSegmentStore, clock: Clock): StrictRouteOffsetSegmentManager {
    return StrictRouteOffsetSegmentManager(
        store = store,
        validator = defaultStrictRouteOffsetSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictRouteOffsetSegmentEngine(),
    )
}
