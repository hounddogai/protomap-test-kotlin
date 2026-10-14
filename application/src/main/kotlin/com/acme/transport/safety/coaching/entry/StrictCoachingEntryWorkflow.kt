package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.clock.Clock

/** Moves strict coaching entries through their life cycle and keeps a journal of what it did. */
data class StrictCoachingEntryService(
    var store: StrictCoachingEntryStore,
    var validator: StrictCoachingEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict coaching entry $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCoachingEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict coaching entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCoachingEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newStrictCoachingEntryService(store: StrictCoachingEntryStore, clock: Clock): StrictCoachingEntryService {
    return StrictCoachingEntryService(
        store = store,
        validator = defaultStrictCoachingEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
