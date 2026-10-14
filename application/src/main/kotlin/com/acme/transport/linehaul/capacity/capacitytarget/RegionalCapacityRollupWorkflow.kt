package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.linehaul.clock.Clock

/** Moves regional capacity rollups through their life cycle and keeps a journal of what it did. */
data class RegionalCapacityRollupManager(
    var store: RegionalCapacityRollupStore,
    var validator: RegionalCapacityRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional capacity rollup $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCapacityRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional capacity rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCapacityRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newRegionalCapacityRollupManager(store: RegionalCapacityRollupStore, clock: Clock): RegionalCapacityRollupManager {
    return RegionalCapacityRollupManager(
        store = store,
        validator = defaultRegionalCapacityRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
