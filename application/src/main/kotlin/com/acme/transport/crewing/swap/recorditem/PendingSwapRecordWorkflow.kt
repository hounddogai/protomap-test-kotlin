package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.clock.Clock

/** Moves pending swap records through their life cycle and keeps a journal of what it did. */
data class PendingSwapRecordDesk(
    var store: PendingSwapRecordStore,
    var validator: PendingSwapRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending swap record $code not found")
        validator.requireValid(item)
        item.moveTo(PendingSwapRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending swap record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingSwapRecordStatus.FAILED)
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

fun newPendingSwapRecordDesk(store: PendingSwapRecordStore, clock: Clock): PendingSwapRecordDesk {
    return PendingSwapRecordDesk(
        store = store,
        validator = defaultPendingSwapRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
