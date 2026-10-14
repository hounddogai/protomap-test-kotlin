package com.acme.transport.fleetops.tire.preference

import com.acme.transport.fleetops.clock.Clock

/** Moves tire preferences through their life cycle and keeps a journal of what it did. */
data class TirePreferenceManager(
    var store: TirePreferenceStore,
    var validator: TirePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tire preference $code not found")
        validator.requireValid(item)
        item.moveTo(TirePreferenceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TirePreferenceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newTirePreferenceManager(store: TirePreferenceStore, clock: Clock): TirePreferenceManager {
    return TirePreferenceManager(
        store = store,
        validator = defaultTirePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
