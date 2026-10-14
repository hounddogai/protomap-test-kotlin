package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.clock.Clock

/** Moves inspection windows through their life cycle and keeps a journal of what it did. */
data class InspectionWindowDesk(
    var store: InspectionWindowStore,
    var validator: InspectionWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection window $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionWindowStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionWindowStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newInspectionWindowDesk(store: InspectionWindowStore, clock: Clock): InspectionWindowDesk {
    return InspectionWindowDesk(
        store = store,
        validator = defaultInspectionWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
