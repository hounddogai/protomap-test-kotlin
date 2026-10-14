package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.clock.Clock

/** Moves lenient ev charge preferences through their life cycle and keeps a journal of what it did. */
data class LenientEvChargePreferenceCoordinator(
    var store: LenientEvChargePreferenceStore,
    var validator: LenientEvChargePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientEvChargePreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient ev charge preference $code not found")
        validator.requireValid(item)
        item.moveTo(LenientEvChargePreferenceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient ev charge preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientEvChargePreferenceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient ev charge preference $code not found")
        val result = engine.run(item)
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

fun newLenientEvChargePreferenceCoordinator(store: LenientEvChargePreferenceStore, clock: Clock): LenientEvChargePreferenceCoordinator {
    return LenientEvChargePreferenceCoordinator(
        store = store,
        validator = defaultLenientEvChargePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientEvChargePreferenceEngine(),
    )
}
