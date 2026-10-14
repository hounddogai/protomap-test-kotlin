package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves fallback backhaul preferences through their life cycle and keeps a journal of what it did. */
data class FallbackBackhaulPreferenceDesk(
    var store: FallbackBackhaulPreferenceStore,
    var validator: FallbackBackhaulPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackBackhaulPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback backhaul preference $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackBackhaulPreferenceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback backhaul preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackBackhaulPreferenceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback backhaul preference $code not found")
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

fun newFallbackBackhaulPreferenceDesk(store: FallbackBackhaulPreferenceStore, clock: Clock, flags: Registry): FallbackBackhaulPreferenceDesk {
    return FallbackBackhaulPreferenceDesk(
        store = store,
        validator = defaultFallbackBackhaulPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackBackhaulPreferenceEngine(flags),
    )
}
