package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.fleetops.clock.Clock

/** Moves regional lease return entries through their life cycle and keeps a journal of what it did. */
data class RegionalLeaseReturnEntryCoordinator(
    var store: RegionalLeaseReturnEntryStore,
    var validator: RegionalLeaseReturnEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalLeaseReturnEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional lease return entry $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLeaseReturnEntryStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lease return entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLeaseReturnEntryStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("regional lease return entry $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newRegionalLeaseReturnEntryCoordinator(store: RegionalLeaseReturnEntryStore, clock: Clock): RegionalLeaseReturnEntryCoordinator {
    return RegionalLeaseReturnEntryCoordinator(
        store = store,
        validator = defaultRegionalLeaseReturnEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalLeaseReturnEntryEngine(),
    )
}
