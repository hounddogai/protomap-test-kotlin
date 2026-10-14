package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.linehaul.clock.Clock

/** Moves relay forecasts through their life cycle and keeps a journal of what it did. */
data class RelayForecastService(
    var store: RelayForecastStore,
    var validator: RelayForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay forecast $code not found")
        validator.requireValid(item)
        item.moveTo(RelayForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("relay forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRelayForecastService(store: RelayForecastStore, clock: Clock): RelayForecastService {
    return RelayForecastService(
        store = store,
        validator = defaultRelayForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
