package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.clock.Clock

/** Moves roster exceptions through their life cycle and keeps a journal of what it did. */
data class RosterExceptionWorkflow(
    var store: RosterExceptionStore,
    var validator: RosterExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("roster exception $code not found")
        validator.requireValid(item)
        item.moveTo(RosterExceptionStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("roster exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterExceptionStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRosterExceptionWorkflow(store: RosterExceptionStore, clock: Clock): RosterExceptionWorkflow {
    return RosterExceptionWorkflow(
        store = store,
        validator = defaultRosterExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
