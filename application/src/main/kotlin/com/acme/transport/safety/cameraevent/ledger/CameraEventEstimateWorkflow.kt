package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event estimates through their life cycle and keeps a journal of what it did. */
data class CameraEventEstimateService(
    var store: CameraEventEstimateStore,
    var validator: CameraEventEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("camera event estimate $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("camera event estimate $code not found")
        val result = engine.price(item)
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

fun newCameraEventEstimateService(store: CameraEventEstimateStore, clock: Clock, flags: Registry): CameraEventEstimateService {
    return CameraEventEstimateService(
        store = store,
        validator = defaultCameraEventEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventEstimateEngine(flags),
    )
}
