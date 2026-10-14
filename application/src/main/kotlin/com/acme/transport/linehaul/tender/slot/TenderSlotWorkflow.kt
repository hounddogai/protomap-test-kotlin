package com.acme.transport.linehaul.tender.slot

import com.acme.transport.linehaul.clock.Clock

/** Moves tender slots through their life cycle and keeps a journal of what it did. */
data class TenderSlotCoordinator(
    var store: TenderSlotStore,
    var validator: TenderSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tender slot $code not found")
        validator.requireValid(item)
        item.moveTo(TenderSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newTenderSlotCoordinator(store: TenderSlotStore, clock: Clock): TenderSlotCoordinator {
    return TenderSlotCoordinator(
        store = store,
        validator = defaultTenderSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
