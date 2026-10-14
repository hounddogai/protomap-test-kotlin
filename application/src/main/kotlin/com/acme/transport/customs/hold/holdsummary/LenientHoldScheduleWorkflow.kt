package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.customs.clock.Clock

/** Moves lenient hold schedules through their life cycle and keeps a journal of what it did. */
data class LenientHoldScheduleCoordinator(
    var store: LenientHoldScheduleStore,
    var validator: LenientHoldScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient hold schedule $code not found")
        validator.requireValid(item)
        item.moveTo(LenientHoldScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient hold schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientHoldScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newLenientHoldScheduleCoordinator(store: LenientHoldScheduleStore, clock: Clock): LenientHoldScheduleCoordinator {
    return LenientHoldScheduleCoordinator(
        store = store,
        validator = defaultLenientHoldScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
