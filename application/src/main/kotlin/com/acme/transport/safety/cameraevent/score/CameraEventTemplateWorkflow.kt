package com.acme.transport.safety.cameraevent.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event templates through their life cycle and keeps a journal of what it did. */
data class CameraEventTemplateManager(
    var store: CameraEventTemplateStore,
    var validator: CameraEventTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("camera event template $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventTemplateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventTemplateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("camera event template $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newCameraEventTemplateManager(store: CameraEventTemplateStore, clock: Clock, flags: Registry): CameraEventTemplateManager {
    return CameraEventTemplateManager(
        store = store,
        validator = defaultCameraEventTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventTemplateEngine(flags),
    )
}
