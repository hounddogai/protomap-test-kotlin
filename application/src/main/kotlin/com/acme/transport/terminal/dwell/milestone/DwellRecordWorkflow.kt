package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.terminal.clock.Clock

/** Moves dwell records through their life cycle and keeps a journal of what it did. */
data class DwellRecordDesk(
    var store: DwellRecordStore,
    var validator: DwellRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell record $code not found")
        validator.requireValid(item)
        item.moveTo(DwellRecordStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellRecordStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newDwellRecordDesk(store: DwellRecordStore, clock: Clock): DwellRecordDesk {
    return DwellRecordDesk(
        store = store,
        validator = defaultDwellRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
