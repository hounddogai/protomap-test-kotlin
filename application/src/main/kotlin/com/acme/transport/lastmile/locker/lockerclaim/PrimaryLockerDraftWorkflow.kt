package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.lastmile.clock.Clock

/** Moves primary locker drafts through their life cycle and keeps a journal of what it did. */
data class PrimaryLockerDraftService(
    var store: PrimaryLockerDraftStore,
    var validator: PrimaryLockerDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary locker draft $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryLockerDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary locker draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryLockerDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newPrimaryLockerDraftService(store: PrimaryLockerDraftStore, clock: Clock): PrimaryLockerDraftService {
    return PrimaryLockerDraftService(
        store = store,
        validator = defaultPrimaryLockerDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
