package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves regional drop preferences through their life cycle and keeps a journal of what it did. */
data class RegionalDropPreferenceService(
    var store: RegionalDropPreferenceStore,
    var validator: RegionalDropPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalDropPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional drop preference $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalDropPreferenceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional drop preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalDropPreferenceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional drop preference $code not found")
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

fun newRegionalDropPreferenceService(store: RegionalDropPreferenceStore, clock: Clock, flags: Registry): RegionalDropPreferenceService {
    return RegionalDropPreferenceService(
        store = store,
        validator = defaultRegionalDropPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalDropPreferenceEngine(flags),
    )
}
