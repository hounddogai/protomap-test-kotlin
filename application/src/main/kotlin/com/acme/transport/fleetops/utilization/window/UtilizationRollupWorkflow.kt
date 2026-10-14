package com.acme.transport.fleetops.utilization.window

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization rollups through their life cycle and keeps a journal of what it did. */
data class UtilizationRollupService(
    var store: UtilizationRollupStore,
    var validator: UtilizationRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization rollup $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newUtilizationRollupService(store: UtilizationRollupStore, clock: Clock): UtilizationRollupService {
    return UtilizationRollupService(
        store = store,
        validator = defaultUtilizationRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
