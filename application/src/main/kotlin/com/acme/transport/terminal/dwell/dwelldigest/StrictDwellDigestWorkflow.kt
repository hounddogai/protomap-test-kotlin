package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.terminal.clock.Clock

/** Moves strict dwell digests through their life cycle and keeps a journal of what it did. */
data class StrictDwellDigestManager(
    var store: StrictDwellDigestStore,
    var validator: StrictDwellDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict dwell digest $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDwellDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict dwell digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDwellDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newStrictDwellDigestManager(store: StrictDwellDigestStore, clock: Clock): StrictDwellDigestManager {
    return StrictDwellDigestManager(
        store = store,
        validator = defaultStrictDwellDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
