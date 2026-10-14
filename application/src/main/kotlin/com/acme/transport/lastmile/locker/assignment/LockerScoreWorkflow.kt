package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.lastmile.clock.Clock

/** Moves locker scores through their life cycle and keeps a journal of what it did. */
data class LockerScoreDesk(
    var store: LockerScoreStore,
    var validator: LockerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("locker score $code not found")
        validator.requireValid(item)
        item.moveTo(LockerScoreStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("locker score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerScoreStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newLockerScoreDesk(store: LockerScoreStore, clock: Clock): LockerScoreDesk {
    return LockerScoreDesk(
        store = store,
        validator = defaultLockerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
