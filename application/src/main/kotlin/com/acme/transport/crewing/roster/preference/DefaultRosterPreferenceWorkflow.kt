package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.clock.Clock

/** Moves default roster preferences through their life cycle and keeps a journal of what it did. */
data class DefaultRosterPreferenceDesk(
    var store: DefaultRosterPreferenceStore,
    var validator: DefaultRosterPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default roster preference $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRosterPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default roster preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRosterPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultRosterPreferenceDesk(store: DefaultRosterPreferenceStore, clock: Clock): DefaultRosterPreferenceDesk {
    return DefaultRosterPreferenceDesk(
        store = store,
        validator = defaultDefaultRosterPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
