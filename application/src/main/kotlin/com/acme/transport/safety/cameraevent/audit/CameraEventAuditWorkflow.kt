package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.safety.clock.Clock

/** Moves camera event audits through their life cycle and keeps a journal of what it did. */
data class CameraEventAuditManager(
    var store: CameraEventAuditStore,
    var validator: CameraEventAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("camera event audit $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCameraEventAuditManager(store: CameraEventAuditStore, clock: Clock): CameraEventAuditManager {
    return CameraEventAuditManager(
        store = store,
        validator = defaultCameraEventAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
