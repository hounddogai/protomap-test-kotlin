package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.safety.clock.Clock

/** Moves camera event quotas through their life cycle and keeps a journal of what it did. */
data class CameraEventQuotaWorkflow(
    var store: CameraEventQuotaStore,
    var validator: CameraEventQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("camera event quota $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newCameraEventQuotaWorkflow(store: CameraEventQuotaStore, clock: Clock): CameraEventQuotaWorkflow {
    return CameraEventQuotaWorkflow(
        store = store,
        validator = defaultCameraEventQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
