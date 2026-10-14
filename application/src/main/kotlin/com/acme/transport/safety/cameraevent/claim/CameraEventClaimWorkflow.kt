package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event claims through their life cycle and keeps a journal of what it did. */
data class CameraEventClaimService(
    var store: CameraEventClaimStore,
    var validator: CameraEventClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("camera event claim $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("camera event claim $code not found")
        val result = engine.evaluate(item)
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

fun newCameraEventClaimService(store: CameraEventClaimStore, clock: Clock, flags: Registry): CameraEventClaimService {
    return CameraEventClaimService(
        store = store,
        validator = defaultCameraEventClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventClaimEngine(flags),
    )
}
