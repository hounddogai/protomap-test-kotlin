package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event targets through their life cycle and keeps a journal of what it did. */
data class CameraEventTargetWorkflow(
    var store: CameraEventTargetStore,
    var validator: CameraEventTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event target $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventTargetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventTargetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("camera event target $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newCameraEventTargetWorkflow(store: CameraEventTargetStore, clock: Clock, flags: Registry): CameraEventTargetWorkflow {
    return CameraEventTargetWorkflow(
        store = store,
        validator = defaultCameraEventTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventTargetEngine(flags),
    )
}
