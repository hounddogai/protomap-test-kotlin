package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.linehaul.clock.Clock

/** Moves pending relay forecasts through their life cycle and keeps a journal of what it did. */
data class PendingRelayForecastWorkflow(
    var store: PendingRelayForecastStore,
    var validator: PendingRelayForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingRelayForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending relay forecast $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRelayForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending relay forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRelayForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending relay forecast $code not found")
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

fun newPendingRelayForecastWorkflow(store: PendingRelayForecastStore, clock: Clock): PendingRelayForecastWorkflow {
    return PendingRelayForecastWorkflow(
        store = store,
        validator = defaultPendingRelayForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingRelayForecastEngine(),
    )
}
