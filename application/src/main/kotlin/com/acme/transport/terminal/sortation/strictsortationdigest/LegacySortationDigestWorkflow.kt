package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.terminal.clock.Clock

/** Moves legacy sortation digests through their life cycle and keeps a journal of what it did. */
data class LegacySortationDigestCoordinator(
    var store: LegacySortationDigestStore,
    var validator: LegacySortationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy sortation digest $code not found")
        validator.requireValid(item)
        item.moveTo(LegacySortationDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy sortation digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacySortationDigestStatus.CANCELLED)
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

fun newLegacySortationDigestCoordinator(store: LegacySortationDigestStore, clock: Clock): LegacySortationDigestCoordinator {
    return LegacySortationDigestCoordinator(
        store = store,
        validator = defaultLegacySortationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
