package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.lastmile.clock.Clock

/** Moves manual drop records through their life cycle and keeps a journal of what it did. */
data class ManualDropRecordManager(
    var store: ManualDropRecordStore,
    var validator: ManualDropRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual drop record $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDropRecordStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual drop record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDropRecordStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newManualDropRecordManager(store: ManualDropRecordStore, clock: Clock): ManualDropRecordManager {
    return ManualDropRecordManager(
        store = store,
        validator = defaultManualDropRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
