package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.lastmile.clock.Clock

/** Moves pending locker records through their life cycle and keeps a journal of what it did. */
data class PendingLockerRecordManager(
    var store: PendingLockerRecordStore,
    var validator: PendingLockerRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending locker record $code not found")
        validator.requireValid(item)
        item.moveTo(PendingLockerRecordStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending locker record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingLockerRecordStatus.RETIRED)
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

fun newPendingLockerRecordManager(store: PendingLockerRecordStore, clock: Clock): PendingLockerRecordManager {
    return PendingLockerRecordManager(
        store = store,
        validator = defaultPendingLockerRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
