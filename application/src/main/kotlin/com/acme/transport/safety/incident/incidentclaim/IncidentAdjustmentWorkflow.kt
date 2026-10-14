package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves incident adjustments through their life cycle and keeps a journal of what it did. */
data class IncidentAdjustmentCoordinator(
    var store: IncidentAdjustmentStore,
    var validator: IncidentAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("incident adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("incident adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("incident adjustment $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newIncidentAdjustmentCoordinator(store: IncidentAdjustmentStore, clock: Clock, flags: Registry): IncidentAdjustmentCoordinator {
    return IncidentAdjustmentCoordinator(
        store = store,
        validator = defaultIncidentAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentAdjustmentEngine(flags),
    )
}
