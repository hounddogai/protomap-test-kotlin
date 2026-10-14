package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel snapshots through their life cycle and keeps a journal of what it did. */
data class ParcelSnapshotWorkflow(
    var store: ParcelSnapshotStore,
    var validator: ParcelSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelSnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelSnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newParcelSnapshotWorkflow(store: ParcelSnapshotStore, clock: Clock): ParcelSnapshotWorkflow {
    return ParcelSnapshotWorkflow(
        store = store,
        validator = defaultParcelSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
