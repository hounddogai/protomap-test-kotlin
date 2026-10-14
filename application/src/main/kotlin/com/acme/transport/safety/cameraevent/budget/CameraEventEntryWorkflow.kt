package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves camera event entries through their life cycle and keeps a journal of what it did. */
data class CameraEventEntryDesk(
    var store: CameraEventEntryStore,
    var validator: CameraEventEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CameraEventEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("camera event entry $code not found")
        validator.requireValid(item)
        item.moveTo(CameraEventEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("camera event entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CameraEventEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("camera event entry $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newCameraEventEntryDesk(store: CameraEventEntryStore, clock: Clock, flags: Registry): CameraEventEntryDesk {
    return CameraEventEntryDesk(
        store = store,
        validator = defaultCameraEventEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCameraEventEntryEngine(flags),
    )
}
