package com.acme.transport.terminal.dwell.schedule

import com.acme.transport.terminal.clock.Clock

/** Moves capped dwell entries through their life cycle and keeps a journal of what it did. */
data class CappedDwellEntryDesk(
    var store: CappedDwellEntryStore,
    var validator: CappedDwellEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped dwell entry $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDwellEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped dwell entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDwellEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newCappedDwellEntryDesk(store: CappedDwellEntryStore, clock: Clock): CappedDwellEntryDesk {
    return CappedDwellEntryDesk(
        store = store,
        validator = defaultCappedDwellEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
