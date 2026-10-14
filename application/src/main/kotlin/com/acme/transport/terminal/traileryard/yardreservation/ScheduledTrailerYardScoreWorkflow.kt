package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.terminal.clock.Clock

/** Moves scheduled trailer yard scores through their life cycle and keeps a journal of what it did. */
data class ScheduledTrailerYardScoreService(
    var store: ScheduledTrailerYardScoreStore,
    var validator: ScheduledTrailerYardScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled trailer yard score $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledTrailerYardScoreStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled trailer yard score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledTrailerYardScoreStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newScheduledTrailerYardScoreService(store: ScheduledTrailerYardScoreStore, clock: Clock): ScheduledTrailerYardScoreService {
    return ScheduledTrailerYardScoreService(
        store = store,
        validator = defaultScheduledTrailerYardScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
