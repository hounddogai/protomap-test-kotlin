package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.linehaul.clock.Clock

/** Moves tiered backhaul preferences through their life cycle and keeps a journal of what it did. */
data class TieredBackhaulPreferenceManager(
    var store: TieredBackhaulPreferenceStore,
    var validator: TieredBackhaulPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered backhaul preference $code not found")
        validator.requireValid(item)
        item.moveTo(TieredBackhaulPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered backhaul preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredBackhaulPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTieredBackhaulPreferenceManager(store: TieredBackhaulPreferenceStore, clock: Clock): TieredBackhaulPreferenceManager {
    return TieredBackhaulPreferenceManager(
        store = store,
        validator = defaultTieredBackhaulPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
