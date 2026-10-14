package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.terminal.clock.Clock

/** Moves trailer yard entries through their life cycle and keeps a journal of what it did. */
data class TrailerYardEntryWorkflow(
    var store: TrailerYardEntryStore,
    var validator: TrailerYardEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("trailer yard entry $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerYardEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer yard entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerYardEntryStatus.RETIRED)
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

fun newTrailerYardEntryWorkflow(store: TrailerYardEntryStore, clock: Clock): TrailerYardEntryWorkflow {
    return TrailerYardEntryWorkflow(
        store = store,
        validator = defaultTrailerYardEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
