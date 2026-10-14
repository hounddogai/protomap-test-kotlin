package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal attempt entries through their life cycle and keeps a journal of what it did. */
data class SeasonalAttemptEntryManager(
    var store: SeasonalAttemptEntryStore,
    var validator: SeasonalAttemptEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal attempt entry $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAttemptEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal attempt entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAttemptEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newSeasonalAttemptEntryManager(store: SeasonalAttemptEntryStore, clock: Clock): SeasonalAttemptEntryManager {
    return SeasonalAttemptEntryManager(
        store = store,
        validator = defaultSeasonalAttemptEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
