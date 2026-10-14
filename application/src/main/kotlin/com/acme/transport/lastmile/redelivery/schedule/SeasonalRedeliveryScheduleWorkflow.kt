package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal redelivery schedules through their life cycle and keeps a journal of what it did. */
data class SeasonalRedeliveryScheduleCoordinator(
    var store: SeasonalRedeliveryScheduleStore,
    var validator: SeasonalRedeliveryScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal redelivery schedule $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRedeliveryScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal redelivery schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRedeliveryScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newSeasonalRedeliveryScheduleCoordinator(store: SeasonalRedeliveryScheduleStore, clock: Clock): SeasonalRedeliveryScheduleCoordinator {
    return SeasonalRedeliveryScheduleCoordinator(
        store = store,
        validator = defaultSeasonalRedeliveryScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
