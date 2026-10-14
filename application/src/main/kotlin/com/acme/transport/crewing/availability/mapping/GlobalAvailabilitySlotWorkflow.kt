package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.clock.Clock

/** Moves global availability slots through their life cycle and keeps a journal of what it did. */
data class GlobalAvailabilitySlotCoordinator(
    var store: GlobalAvailabilitySlotStore,
    var validator: GlobalAvailabilitySlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global availability slot $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAvailabilitySlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global availability slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAvailabilitySlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newGlobalAvailabilitySlotCoordinator(store: GlobalAvailabilitySlotStore, clock: Clock): GlobalAvailabilitySlotCoordinator {
    return GlobalAvailabilitySlotCoordinator(
        store = store,
        validator = defaultGlobalAvailabilitySlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
