package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.clock.Clock

/** Moves camera event variances through their life cycle and keeps a journal of what it did. */
data class CameraEventVarianceWorkflow(
    var store: CameraEventVarianceStore,
    var validator: CameraEventVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("camera event variance $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventVarianceStatus.STOPPED)
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

fun newCameraEventVarianceWorkflow(store: CameraEventVarianceStore, clock: Clock): CameraEventVarianceWorkflow {
    return CameraEventVarianceWorkflow(
        store = store,
        validator = defaultCameraEventVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
