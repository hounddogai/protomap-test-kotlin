package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.clock.Clock

/** Moves clearance forecasts through their life cycle and keeps a journal of what it did. */
data class ClearanceForecastWorkflow(
    var store: ClearanceForecastStore,
    var validator: ClearanceForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("clearance forecast $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceForecastStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceForecastStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newClearanceForecastWorkflow(store: ClearanceForecastStore, clock: Clock): ClearanceForecastWorkflow {
    return ClearanceForecastWorkflow(
        store = store,
        validator = defaultClearanceForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
