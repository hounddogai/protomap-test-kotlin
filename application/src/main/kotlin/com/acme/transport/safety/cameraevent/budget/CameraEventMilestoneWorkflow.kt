package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.safety.clock.Clock

/** Moves camera event milestones through their life cycle and keeps a journal of what it did. */
data class CameraEventMilestoneManager(
    var store: CameraEventMilestoneStore,
    var validator: CameraEventMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("camera event milestone $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("camera event milestone $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newCameraEventMilestoneManager(store: CameraEventMilestoneStore, clock: Clock): CameraEventMilestoneManager {
    return CameraEventMilestoneManager(
        store = store,
        validator = defaultCameraEventMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventMilestoneEngine(),
    )
}
