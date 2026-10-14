package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity preferences through their life cycle and keeps a journal of what it did. */
data class CapacityPreferenceService(
    var store: CapacityPreferenceStore,
    var validator: CapacityPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capacity preference $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityPreferenceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityPreferenceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newCapacityPreferenceService(store: CapacityPreferenceStore, clock: Clock): CapacityPreferenceService {
    return CapacityPreferenceService(
        store = store,
        validator = defaultCapacityPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
