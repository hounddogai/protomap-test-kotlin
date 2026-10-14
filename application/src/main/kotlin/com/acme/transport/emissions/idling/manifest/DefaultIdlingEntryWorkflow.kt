package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves default idling entries through their life cycle and keeps a journal of what it did. */
data class DefaultIdlingEntryService(
    var store: DefaultIdlingEntryStore,
    var validator: DefaultIdlingEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default idling entry $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultIdlingEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default idling entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultIdlingEntryStatus.RETIRED)
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

fun newDefaultIdlingEntryService(store: DefaultIdlingEntryStore, clock: Clock): DefaultIdlingEntryService {
    return DefaultIdlingEntryService(
        store = store,
        validator = defaultDefaultIdlingEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
