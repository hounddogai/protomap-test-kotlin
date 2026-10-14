package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.lastmile.clock.Clock

/** Moves global redelivery snapshots through their life cycle and keeps a journal of what it did. */
data class GlobalRedeliverySnapshotDesk(
    var store: GlobalRedeliverySnapshotStore,
    var validator: GlobalRedeliverySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global redelivery snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRedeliverySnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global redelivery snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRedeliverySnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalRedeliverySnapshotDesk(store: GlobalRedeliverySnapshotStore, clock: Clock): GlobalRedeliverySnapshotDesk {
    return GlobalRedeliverySnapshotDesk(
        store = store,
        validator = defaultGlobalRedeliverySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
