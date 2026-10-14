package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves global incident requests through their life cycle and keeps a journal of what it did. */
data class GlobalIncidentRequestCoordinator(
    var store: GlobalIncidentRequestStore,
    var validator: GlobalIncidentRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalIncidentRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global incident request $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalIncidentRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global incident request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalIncidentRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global incident request $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalIncidentRequestCoordinator(store: GlobalIncidentRequestStore, clock: Clock, flags: Registry): GlobalIncidentRequestCoordinator {
    return GlobalIncidentRequestCoordinator(
        store = store,
        validator = defaultGlobalIncidentRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalIncidentRequestEngine(flags),
    )
}
