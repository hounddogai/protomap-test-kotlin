package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.clock.Clock

/** Moves tiered camera event forecasts through their life cycle and keeps a journal of what it did. */
data class TieredCameraEventForecastManager(
    var store: TieredCameraEventForecastStore,
    var validator: TieredCameraEventForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered camera event forecast $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCameraEventForecastStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered camera event forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCameraEventForecastStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newTieredCameraEventForecastManager(store: TieredCameraEventForecastStore, clock: Clock): TieredCameraEventForecastManager {
    return TieredCameraEventForecastManager(
        store = store,
        validator = defaultTieredCameraEventForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
