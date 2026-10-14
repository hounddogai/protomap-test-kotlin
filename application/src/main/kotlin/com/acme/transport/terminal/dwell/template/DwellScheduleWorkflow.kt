package com.acme.transport.terminal.dwell.template

import com.acme.transport.terminal.clock.Clock

/** Moves dwell schedules through their life cycle and keeps a journal of what it did. */
data class DwellScheduleService(
    var store: DwellScheduleStore,
    var validator: DwellScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dwell schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DwellScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellScheduleStatus.ARCHIVED)
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

fun newDwellScheduleService(store: DwellScheduleStore, clock: Clock): DwellScheduleService {
    return DwellScheduleService(
        store = store,
        validator = defaultDwellScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
