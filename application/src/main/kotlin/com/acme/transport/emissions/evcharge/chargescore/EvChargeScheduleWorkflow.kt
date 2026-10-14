package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge schedules through their life cycle and keeps a journal of what it did. */
data class EvChargeScheduleService(
    var store: EvChargeScheduleStore,
    var validator: EvChargeScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("ev charge schedule $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newEvChargeScheduleService(store: EvChargeScheduleStore, clock: Clock): EvChargeScheduleService {
    return EvChargeScheduleService(
        store = store,
        validator = defaultEvChargeScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
