package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.terminal.clock.Clock

/** Moves primary trailer yard cycles through their life cycle and keeps a journal of what it did. */
data class PrimaryTrailerYardCycleService(
    var store: PrimaryTrailerYardCycleStore,
    var validator: PrimaryTrailerYardCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary trailer yard cycle $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTrailerYardCycleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary trailer yard cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTrailerYardCycleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPrimaryTrailerYardCycleService(store: PrimaryTrailerYardCycleStore, clock: Clock): PrimaryTrailerYardCycleService {
    return PrimaryTrailerYardCycleService(
        store = store,
        validator = defaultPrimaryTrailerYardCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
