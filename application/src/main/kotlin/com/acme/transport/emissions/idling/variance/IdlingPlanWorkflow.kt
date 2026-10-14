package com.acme.transport.emissions.idling.variance

import com.acme.transport.emissions.clock.Clock

/** Moves idling plans through their life cycle and keeps a journal of what it did. */
data class IdlingPlanDesk(
    var store: IdlingPlanStore,
    var validator: IdlingPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("idling plan $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling plan $code not found")
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

fun newIdlingPlanDesk(store: IdlingPlanStore, clock: Clock): IdlingPlanDesk {
    return IdlingPlanDesk(
        store = store,
        validator = defaultIdlingPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingPlanEngine(),
    )
}
