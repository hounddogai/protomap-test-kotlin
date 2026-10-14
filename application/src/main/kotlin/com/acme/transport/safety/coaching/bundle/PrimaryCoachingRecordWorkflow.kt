package com.acme.transport.safety.coaching.bundle

import com.acme.transport.safety.clock.Clock

/** Moves primary coaching records through their life cycle and keeps a journal of what it did. */
data class PrimaryCoachingRecordDesk(
    var store: PrimaryCoachingRecordStore,
    var validator: PrimaryCoachingRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary coaching record $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCoachingRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary coaching record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCoachingRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newPrimaryCoachingRecordDesk(store: PrimaryCoachingRecordStore, clock: Clock): PrimaryCoachingRecordDesk {
    return PrimaryCoachingRecordDesk(
        store = store,
        validator = defaultPrimaryCoachingRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
