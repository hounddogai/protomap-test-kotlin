package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.safety.clock.Clock

/** Moves regional incident exceptions through their life cycle and keeps a journal of what it did. */
data class RegionalIncidentExceptionCoordinator(
    var store: RegionalIncidentExceptionStore,
    var validator: RegionalIncidentExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional incident exception $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalIncidentExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional incident exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalIncidentExceptionStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRegionalIncidentExceptionCoordinator(store: RegionalIncidentExceptionStore, clock: Clock): RegionalIncidentExceptionCoordinator {
    return RegionalIncidentExceptionCoordinator(
        store = store,
        validator = defaultRegionalIncidentExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
