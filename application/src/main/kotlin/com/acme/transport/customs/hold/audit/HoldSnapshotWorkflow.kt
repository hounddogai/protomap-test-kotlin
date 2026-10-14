package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.clock.Clock

/** Moves hold snapshots through their life cycle and keeps a journal of what it did. */
data class HoldSnapshotWorkflow(
    var store: HoldSnapshotStore,
    var validator: HoldSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(HoldSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("hold snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newHoldSnapshotWorkflow(store: HoldSnapshotStore, clock: Clock): HoldSnapshotWorkflow {
    return HoldSnapshotWorkflow(
        store = store,
        validator = defaultHoldSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
