package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.safety.clock.Clock

/** Moves lenient near miss schedules through their life cycle and keeps a journal of what it did. */
data class LenientNearMissScheduleWorkflow(
    var store: LenientNearMissScheduleStore,
    var validator: LenientNearMissScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient near miss schedule $code not found")
        validator.requireValid(item)
        item.moveTo(LenientNearMissScheduleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient near miss schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientNearMissScheduleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientNearMissScheduleWorkflow(store: LenientNearMissScheduleStore, clock: Clock): LenientNearMissScheduleWorkflow {
    return LenientNearMissScheduleWorkflow(
        store = store,
        validator = defaultLenientNearMissScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
