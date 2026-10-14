package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.lastmile.clock.Clock

/** Moves global attempt records through their life cycle and keeps a journal of what it did. */
data class GlobalAttemptRecordService(
    var store: GlobalAttemptRecordStore,
    var validator: GlobalAttemptRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global attempt record $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAttemptRecordStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global attempt record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAttemptRecordStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newGlobalAttemptRecordService(store: GlobalAttemptRecordStore, clock: Clock): GlobalAttemptRecordService {
    return GlobalAttemptRecordService(
        store = store,
        validator = defaultGlobalAttemptRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
