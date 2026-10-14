package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event digests through their life cycle and keeps a journal of what it did. */
data class CameraEventDigestDesk(
    var store: CameraEventDigestStore,
    var validator: CameraEventDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event digest $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("camera event digest $code not found")
        val result = engine.evaluate(item)
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

fun newCameraEventDigestDesk(store: CameraEventDigestStore, clock: Clock, flags: Registry): CameraEventDigestDesk {
    return CameraEventDigestDesk(
        store = store,
        validator = defaultCameraEventDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventDigestEngine(flags),
    )
}
