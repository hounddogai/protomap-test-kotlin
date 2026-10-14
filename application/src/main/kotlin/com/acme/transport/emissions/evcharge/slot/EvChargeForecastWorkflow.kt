package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge forecasts through their life cycle and keeps a journal of what it did. */
data class EvChargeForecastCoordinator(
    var store: EvChargeForecastStore,
    var validator: EvChargeForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("ev charge forecast $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeForecastStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeForecastStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newEvChargeForecastCoordinator(store: EvChargeForecastStore, clock: Clock): EvChargeForecastCoordinator {
    return EvChargeForecastCoordinator(
        store = store,
        validator = defaultEvChargeForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
