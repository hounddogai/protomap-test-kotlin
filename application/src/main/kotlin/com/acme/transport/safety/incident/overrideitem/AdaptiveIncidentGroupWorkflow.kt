package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.safety.clock.Clock

/** Moves adaptive incident groups through their life cycle and keeps a journal of what it did. */
data class AdaptiveIncidentGroupCoordinator(
    var store: AdaptiveIncidentGroupStore,
    var validator: AdaptiveIncidentGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveIncidentGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive incident group $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveIncidentGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive incident group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveIncidentGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive incident group $code not found")
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

fun newAdaptiveIncidentGroupCoordinator(store: AdaptiveIncidentGroupStore, clock: Clock): AdaptiveIncidentGroupCoordinator {
    return AdaptiveIncidentGroupCoordinator(
        store = store,
        validator = defaultAdaptiveIncidentGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveIncidentGroupEngine(),
    )
}
