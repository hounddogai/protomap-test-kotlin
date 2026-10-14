package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.safety.clock.Clock

/** Moves manual camera event slots through their life cycle and keeps a journal of what it did. */
data class ManualCameraEventSlotWorkflow(
    var store: ManualCameraEventSlotStore,
    var validator: ManualCameraEventSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualCameraEventSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual camera event slot $code not found")
        validator.requireValid(item)
        item.moveTo(ManualCameraEventSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual camera event slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualCameraEventSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual camera event slot $code not found")
        val result = engine.compute(item)
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

fun newManualCameraEventSlotWorkflow(store: ManualCameraEventSlotStore, clock: Clock): ManualCameraEventSlotWorkflow {
    return ManualCameraEventSlotWorkflow(
        store = store,
        validator = defaultManualCameraEventSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualCameraEventSlotEngine(),
    )
}
