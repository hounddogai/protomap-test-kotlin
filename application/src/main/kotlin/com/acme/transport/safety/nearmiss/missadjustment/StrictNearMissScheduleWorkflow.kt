package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.safety.clock.Clock

/** Moves strict near miss schedules through their life cycle and keeps a journal of what it did. */
data class StrictNearMissScheduleWorkflow(
    var store: StrictNearMissScheduleStore,
    var validator: StrictNearMissScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict near miss schedule $code not found")
        validator.requireValid(item)
        item.moveTo(StrictNearMissScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict near miss schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictNearMissScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newStrictNearMissScheduleWorkflow(store: StrictNearMissScheduleStore, clock: Clock): StrictNearMissScheduleWorkflow {
    return StrictNearMissScheduleWorkflow(
        store = store,
        validator = defaultStrictNearMissScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
