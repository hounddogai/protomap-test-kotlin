package com.acme.transport.network.zone.schedule

import com.acme.transport.network.clock.Clock

/** Moves global zone schedules through their life cycle and keeps a journal of what it did. */
data class GlobalZoneScheduleService(
    var store: GlobalZoneScheduleStore,
    var validator: GlobalZoneScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global zone schedule $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalZoneScheduleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global zone schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalZoneScheduleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newGlobalZoneScheduleService(store: GlobalZoneScheduleStore, clock: Clock): GlobalZoneScheduleService {
    return GlobalZoneScheduleService(
        store = store,
        validator = defaultGlobalZoneScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
