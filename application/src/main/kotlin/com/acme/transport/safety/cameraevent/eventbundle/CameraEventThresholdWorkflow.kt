package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.safety.clock.Clock

/** Moves camera event thresholds through their life cycle and keeps a journal of what it did. */
data class CameraEventThresholdDesk(
    var store: CameraEventThresholdStore,
    var validator: CameraEventThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event threshold $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventThresholdStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventThresholdStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("camera event threshold $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newCameraEventThresholdDesk(store: CameraEventThresholdStore, clock: Clock): CameraEventThresholdDesk {
    return CameraEventThresholdDesk(
        store = store,
        validator = defaultCameraEventThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventThresholdEngine(),
    )
}
