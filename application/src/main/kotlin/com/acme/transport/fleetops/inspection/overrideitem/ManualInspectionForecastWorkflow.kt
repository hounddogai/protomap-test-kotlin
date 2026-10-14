package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.clock.Clock

/** Moves manual inspection forecasts through their life cycle and keeps a journal of what it did. */
data class ManualInspectionForecastCoordinator(
    var store: ManualInspectionForecastStore,
    var validator: ManualInspectionForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual inspection forecast $code not found")
        validator.requireValid(item)
        item.moveTo(ManualInspectionForecastStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual inspection forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualInspectionForecastStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newManualInspectionForecastCoordinator(store: ManualInspectionForecastStore, clock: Clock): ManualInspectionForecastCoordinator {
    return ManualInspectionForecastCoordinator(
        store = store,
        validator = defaultManualInspectionForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
