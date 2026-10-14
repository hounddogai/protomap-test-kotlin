package com.acme.transport.customs.clearance.preference

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance preferences through their life cycle and keeps a journal of what it did. */
data class ClearancePreferenceManager(
    var store: ClearancePreferenceStore,
    var validator: ClearancePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearancePreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("clearance preference $code not found")
        validator.requireValid(item)
        item.moveTo(ClearancePreferenceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearancePreferenceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("clearance preference $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newClearancePreferenceManager(store: ClearancePreferenceStore, clock: Clock, flags: Registry): ClearancePreferenceManager {
    return ClearancePreferenceManager(
        store = store,
        validator = defaultClearancePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearancePreferenceEngine(flags),
    )
}
