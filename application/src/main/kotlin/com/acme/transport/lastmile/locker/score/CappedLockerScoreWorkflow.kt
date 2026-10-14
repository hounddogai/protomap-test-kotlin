package com.acme.transport.lastmile.locker.score

import com.acme.transport.lastmile.clock.Clock

/** Moves capped locker scores through their life cycle and keeps a journal of what it did. */
data class CappedLockerScoreService(
    var store: CappedLockerScoreStore,
    var validator: CappedLockerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped locker score $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLockerScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped locker score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLockerScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCappedLockerScoreService(store: CappedLockerScoreStore, clock: Clock): CappedLockerScoreService {
    return CappedLockerScoreService(
        store = store,
        validator = defaultCappedLockerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
