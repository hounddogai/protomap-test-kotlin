package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge preferences through their life cycle and keeps a journal of what it did. */
data class EvChargePreferenceManager(
    var store: EvChargePreferenceStore,
    var validator: EvChargePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargePreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("ev charge preference $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargePreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargePreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("ev charge preference $code not found")
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

fun newEvChargePreferenceManager(store: EvChargePreferenceStore, clock: Clock): EvChargePreferenceManager {
    return EvChargePreferenceManager(
        store = store,
        validator = defaultEvChargePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargePreferenceEngine(),
    )
}
