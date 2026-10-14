package com.acme.transport.safety.incident.segment

import com.acme.transport.safety.clock.Clock

/** Moves incident exceptions through their life cycle and keeps a journal of what it did. */
data class IncidentExceptionManager(
    var store: IncidentExceptionStore,
    var validator: IncidentExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IncidentExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("incident exception $code not found")
        validator.requireValid(item)
        item.moveTo(IncidentExceptionStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("incident exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IncidentExceptionStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("incident exception $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newIncidentExceptionManager(store: IncidentExceptionStore, clock: Clock): IncidentExceptionManager {
    return IncidentExceptionManager(
        store = store,
        validator = defaultIncidentExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIncidentExceptionEngine(),
    )
}
