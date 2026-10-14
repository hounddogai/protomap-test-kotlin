package com.acme.transport.terminal.dwell.slot

import com.acme.transport.terminal.clock.Clock

/** Moves rolling dwell slots through their life cycle and keeps a journal of what it did. */
data class RollingDwellSlotService(
    var store: RollingDwellSlotStore,
    var validator: RollingDwellSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling dwell slot $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDwellSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dwell slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDwellSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRollingDwellSlotService(store: RollingDwellSlotStore, clock: Clock): RollingDwellSlotService {
    return RollingDwellSlotService(
        store = store,
        validator = defaultRollingDwellSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
