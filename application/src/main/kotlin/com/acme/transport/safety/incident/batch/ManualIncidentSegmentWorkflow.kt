package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves manual incident segments through their life cycle and keeps a journal of what it did. */
data class ManualIncidentSegmentService(
    var store: ManualIncidentSegmentStore,
    var validator: ManualIncidentSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualIncidentSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual incident segment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualIncidentSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual incident segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualIncidentSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual incident segment $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newManualIncidentSegmentService(store: ManualIncidentSegmentStore, clock: Clock, flags: Registry): ManualIncidentSegmentService {
    return ManualIncidentSegmentService(
        store = store,
        validator = defaultManualIncidentSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualIncidentSegmentEngine(flags),
    )
}
