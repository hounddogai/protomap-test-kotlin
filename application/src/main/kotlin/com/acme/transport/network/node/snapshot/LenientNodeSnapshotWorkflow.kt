package com.acme.transport.network.node.snapshot

import com.acme.transport.network.clock.Clock

/** Moves lenient node snapshots through their life cycle and keeps a journal of what it did. */
data class LenientNodeSnapshotManager(
    var store: LenientNodeSnapshotStore,
    var validator: LenientNodeSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient node snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientNodeSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient node snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientNodeSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newLenientNodeSnapshotManager(store: LenientNodeSnapshotStore, clock: Clock): LenientNodeSnapshotManager {
    return LenientNodeSnapshotManager(
        store = store,
        validator = defaultLenientNodeSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
