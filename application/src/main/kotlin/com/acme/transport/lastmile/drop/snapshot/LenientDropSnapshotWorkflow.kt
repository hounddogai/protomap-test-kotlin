package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient drop snapshots through their life cycle and keeps a journal of what it did. */
data class LenientDropSnapshotDesk(
    var store: LenientDropSnapshotStore,
    var validator: LenientDropSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient drop snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDropSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient drop snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDropSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newLenientDropSnapshotDesk(store: LenientDropSnapshotStore, clock: Clock): LenientDropSnapshotDesk {
    return LenientDropSnapshotDesk(
        store = store,
        validator = defaultLenientDropSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
