package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event requests through their life cycle and keeps a journal of what it did. */
data class CameraEventRequestCoordinator(
    var store: CameraEventRequestStore,
    var validator: CameraEventRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event request $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventRequestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventRequestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("camera event request $code not found")
        val result = engine.price(item)
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

fun newCameraEventRequestCoordinator(store: CameraEventRequestStore, clock: Clock, flags: Registry): CameraEventRequestCoordinator {
    return CameraEventRequestCoordinator(
        store = store,
        validator = defaultCameraEventRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventRequestEngine(flags),
    )
}
