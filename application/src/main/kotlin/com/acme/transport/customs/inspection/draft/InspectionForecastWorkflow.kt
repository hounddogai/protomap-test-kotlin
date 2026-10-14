package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.Clock

/** Moves inspection forecasts through their life cycle and keeps a journal of what it did. */
data class InspectionForecastManager(
    var store: InspectionForecastStore,
    var validator: InspectionForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection forecast $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionForecastStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionForecastStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newInspectionForecastManager(store: InspectionForecastStore, clock: Clock): InspectionForecastManager {
    return InspectionForecastManager(
        store = store,
        validator = defaultInspectionForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
