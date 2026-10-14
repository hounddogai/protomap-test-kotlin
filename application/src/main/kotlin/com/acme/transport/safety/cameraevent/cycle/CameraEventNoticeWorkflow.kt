package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.safety.clock.Clock

/** Moves camera event notices through their life cycle and keeps a journal of what it did. */
data class CameraEventNoticeCoordinator(
    var store: CameraEventNoticeStore,
    var validator: CameraEventNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event notice $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newCameraEventNoticeCoordinator(store: CameraEventNoticeStore, clock: Clock): CameraEventNoticeCoordinator {
    return CameraEventNoticeCoordinator(
        store = store,
        validator = defaultCameraEventNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
