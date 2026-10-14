package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves adaptive camera event records through their life cycle and keeps a journal of what it did. */
data class AdaptiveCameraEventRecordCoordinator(
    var store: AdaptiveCameraEventRecordStore,
    var validator: AdaptiveCameraEventRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveCameraEventRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive camera event record $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveCameraEventRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive camera event record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveCameraEventRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive camera event record $code not found")
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

fun newAdaptiveCameraEventRecordCoordinator(store: AdaptiveCameraEventRecordStore, clock: Clock, flags: Registry): AdaptiveCameraEventRecordCoordinator {
    return AdaptiveCameraEventRecordCoordinator(
        store = store,
        validator = defaultAdaptiveCameraEventRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveCameraEventRecordEngine(flags),
    )
}
