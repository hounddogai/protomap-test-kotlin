package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.safety.clock.Clock

/** Moves near miss forecasts through their life cycle and keeps a journal of what it did. */
data class NearMissForecastManager(
    var store: NearMissForecastStore,
    var validator: NearMissForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("near miss forecast $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissForecastStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissForecastStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("near miss forecast $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newNearMissForecastManager(store: NearMissForecastStore, clock: Clock): NearMissForecastManager {
    return NearMissForecastManager(
        store = store,
        validator = defaultNearMissForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissForecastEngine(),
    )
}
