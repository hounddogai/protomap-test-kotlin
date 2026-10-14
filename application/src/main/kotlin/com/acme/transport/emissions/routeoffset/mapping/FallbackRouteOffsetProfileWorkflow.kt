package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback route offset profiles through their life cycle and keeps a journal of what it did.
 */
data class FallbackRouteOffsetProfileDesk(
    var store: FallbackRouteOffsetProfileStore,
    var validator: FallbackRouteOffsetProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackRouteOffsetProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback route offset profile $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackRouteOffsetProfileStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback route offset profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackRouteOffsetProfileStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback route offset profile $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackRouteOffsetProfileDesk(store: FallbackRouteOffsetProfileStore, clock: Clock, flags: Registry): FallbackRouteOffsetProfileDesk {
    return FallbackRouteOffsetProfileDesk(
        store = store,
        validator = defaultFallbackRouteOffsetProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackRouteOffsetProfileEngine(flags),
    )
}
