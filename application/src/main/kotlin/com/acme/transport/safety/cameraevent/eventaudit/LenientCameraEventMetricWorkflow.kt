package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.clock.Clock

/** Moves lenient camera event metrics through their life cycle and keeps a journal of what it did. */
data class LenientCameraEventMetricService(
    var store: LenientCameraEventMetricStore,
    var validator: LenientCameraEventMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCameraEventMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient camera event metric $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCameraEventMetricStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient camera event metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCameraEventMetricStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient camera event metric $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientCameraEventMetricService(store: LenientCameraEventMetricStore, clock: Clock): LenientCameraEventMetricService {
    return LenientCameraEventMetricService(
        store = store,
        validator = defaultLenientCameraEventMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCameraEventMetricEngine(),
    )
}
