package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.clock.Clock

/** Moves lenient hold slots through their life cycle and keeps a journal of what it did. */
data class LenientHoldSlotService(
    var store: LenientHoldSlotStore,
    var validator: LenientHoldSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient hold slot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientHoldSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient hold slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientHoldSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLenientHoldSlotService(store: LenientHoldSlotStore, clock: Clock): LenientHoldSlotService {
    return LenientHoldSlotService(
        store = store,
        validator = defaultLenientHoldSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
