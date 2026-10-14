package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event assignments through their life cycle and keeps a journal of what it did. */
data class CameraEventAssignmentDesk(
    var store: CameraEventAssignmentStore,
    var validator: CameraEventAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("camera event assignment $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("camera event assignment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newCameraEventAssignmentDesk(store: CameraEventAssignmentStore, clock: Clock, flags: Registry): CameraEventAssignmentDesk {
    return CameraEventAssignmentDesk(
        store = store,
        validator = defaultCameraEventAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventAssignmentEngine(flags),
    )
}
