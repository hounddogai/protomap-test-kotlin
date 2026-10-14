package com.acme.transport.customs.clearance.rollup

import com.acme.transport.customs.clock.Clock

/** Moves regional clearance rollups through their life cycle and keeps a journal of what it did. */
data class RegionalClearanceRollupService(
    var store: RegionalClearanceRollupStore,
    var validator: RegionalClearanceRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional clearance rollup $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalClearanceRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional clearance rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalClearanceRollupStatus.CANCELLED)
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

fun newRegionalClearanceRollupService(store: RegionalClearanceRollupStore, clock: Clock): RegionalClearanceRollupService {
    return RegionalClearanceRollupService(
        store = store,
        validator = defaultRegionalClearanceRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
