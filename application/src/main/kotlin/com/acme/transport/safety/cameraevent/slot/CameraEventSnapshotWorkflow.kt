package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.safety.clock.Clock

/** Moves camera event snapshots through their life cycle and keeps a journal of what it did. */
data class CameraEventSnapshotWorkflow(
    var store: CameraEventSnapshotStore,
    var validator: CameraEventSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("camera event snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCameraEventSnapshotWorkflow(store: CameraEventSnapshotStore, clock: Clock): CameraEventSnapshotWorkflow {
    return CameraEventSnapshotWorkflow(
        store = store,
        validator = defaultCameraEventSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
