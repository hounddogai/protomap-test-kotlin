package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event rollups through their life cycle and keeps a journal of what it did. */
data class CameraEventRollupCoordinator(
    var store: CameraEventRollupStore,
    var validator: CameraEventRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("camera event rollup $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("camera event rollup $code not found")
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

fun newCameraEventRollupCoordinator(store: CameraEventRollupStore, clock: Clock, flags: Registry): CameraEventRollupCoordinator {
    return CameraEventRollupCoordinator(
        store = store,
        validator = defaultCameraEventRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventRollupEngine(flags),
    )
}
