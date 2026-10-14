package com.acme.transport.safety.coaching.segment

import com.acme.transport.safety.clock.Clock

/** Moves strict coaching schedules through their life cycle and keeps a journal of what it did. */
data class StrictCoachingScheduleWorkflow(
    var store: StrictCoachingScheduleStore,
    var validator: StrictCoachingScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict coaching schedule $code not found")
        validator.requireValid(item)
        item.moveTo(StrictCoachingScheduleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict coaching schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictCoachingScheduleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newStrictCoachingScheduleWorkflow(store: StrictCoachingScheduleStore, clock: Clock): StrictCoachingScheduleWorkflow {
    return StrictCoachingScheduleWorkflow(
        store = store,
        validator = defaultStrictCoachingScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
