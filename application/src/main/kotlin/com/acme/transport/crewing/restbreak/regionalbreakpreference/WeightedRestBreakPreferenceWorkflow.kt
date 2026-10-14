package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.clock.Clock

/**
 * Moves weighted rest break preferences through their life cycle and keeps a journal of what it did.
 */
data class WeightedRestBreakPreferenceManager(
    var store: WeightedRestBreakPreferenceStore,
    var validator: WeightedRestBreakPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("weighted rest break preference $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedRestBreakPreferenceStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted rest break preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedRestBreakPreferenceStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newWeightedRestBreakPreferenceManager(store: WeightedRestBreakPreferenceStore, clock: Clock): WeightedRestBreakPreferenceManager {
    return WeightedRestBreakPreferenceManager(
        store = store,
        validator = defaultWeightedRestBreakPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
