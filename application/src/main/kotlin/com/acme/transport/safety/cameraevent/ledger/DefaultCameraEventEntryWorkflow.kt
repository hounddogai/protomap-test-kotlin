package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves default camera event entries through their life cycle and keeps a journal of what it did. */
data class DefaultCameraEventEntryCoordinator(
    var store: DefaultCameraEventEntryStore,
    var validator: DefaultCameraEventEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultCameraEventEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default camera event entry $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultCameraEventEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default camera event entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultCameraEventEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default camera event entry $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newDefaultCameraEventEntryCoordinator(store: DefaultCameraEventEntryStore, clock: Clock, flags: Registry): DefaultCameraEventEntryCoordinator {
    return DefaultCameraEventEntryCoordinator(
        store = store,
        validator = defaultDefaultCameraEventEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultCameraEventEntryEngine(flags),
    )
}
