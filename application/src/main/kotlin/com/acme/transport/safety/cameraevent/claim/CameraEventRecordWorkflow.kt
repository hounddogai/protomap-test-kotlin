package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.clock.Clock

/** Moves camera event records through their life cycle and keeps a journal of what it did. */
data class CameraEventRecordService(
    var store: CameraEventRecordStore,
    var validator: CameraEventRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("camera event record $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventRecordStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventRecordStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newCameraEventRecordService(store: CameraEventRecordStore, clock: Clock): CameraEventRecordService {
    return CameraEventRecordService(
        store = store,
        validator = defaultCameraEventRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
