package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.clock.Clock

/** Moves camera event adjustments through their life cycle and keeps a journal of what it did. */
data class CameraEventAdjustmentCoordinator(
    var store: CameraEventAdjustmentStore,
    var validator: CameraEventAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("camera event adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newCameraEventAdjustmentCoordinator(store: CameraEventAdjustmentStore, clock: Clock): CameraEventAdjustmentCoordinator {
    return CameraEventAdjustmentCoordinator(
        store = store,
        validator = defaultCameraEventAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
