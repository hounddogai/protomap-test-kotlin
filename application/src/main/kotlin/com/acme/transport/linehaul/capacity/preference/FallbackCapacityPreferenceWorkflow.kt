package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves fallback capacity preferences through their life cycle and keeps a journal of what it did. */
data class FallbackCapacityPreferenceDesk(
    var store: FallbackCapacityPreferenceStore,
    var validator: FallbackCapacityPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCapacityPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback capacity preference $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCapacityPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback capacity preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCapacityPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback capacity preference $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newFallbackCapacityPreferenceDesk(store: FallbackCapacityPreferenceStore, clock: Clock, flags: Registry): FallbackCapacityPreferenceDesk {
    return FallbackCapacityPreferenceDesk(
        store = store,
        validator = defaultFallbackCapacityPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCapacityPreferenceEngine(flags),
    )
}
