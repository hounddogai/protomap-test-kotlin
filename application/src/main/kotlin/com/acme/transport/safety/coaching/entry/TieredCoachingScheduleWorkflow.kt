package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.clock.Clock

/** Moves tiered coaching schedules through their life cycle and keeps a journal of what it did. */
data class TieredCoachingScheduleCoordinator(
    var store: TieredCoachingScheduleStore,
    var validator: TieredCoachingScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered coaching schedule $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCoachingScheduleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered coaching schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCoachingScheduleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newTieredCoachingScheduleCoordinator(store: TieredCoachingScheduleStore, clock: Clock): TieredCoachingScheduleCoordinator {
    return TieredCoachingScheduleCoordinator(
        store = store,
        validator = defaultTieredCoachingScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
