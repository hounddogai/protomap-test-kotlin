package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event agreements through their life cycle and keeps a journal of what it did. */
data class CameraEventAgreementCoordinator(
    var store: CameraEventAgreementStore,
    var validator: CameraEventAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event agreement $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("camera event agreement $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newCameraEventAgreementCoordinator(store: CameraEventAgreementStore, clock: Clock, flags: Registry): CameraEventAgreementCoordinator {
    return CameraEventAgreementCoordinator(
        store = store,
        validator = defaultCameraEventAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventAgreementEngine(flags),
    )
}
