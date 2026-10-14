package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.Clock

/** Moves weighted inspection rollups through their life cycle and keeps a journal of what it did. */
data class WeightedInspectionRollupManager(
    var store: WeightedInspectionRollupStore,
    var validator: WeightedInspectionRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted inspection rollup $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedInspectionRollupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted inspection rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedInspectionRollupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newWeightedInspectionRollupManager(store: WeightedInspectionRollupStore, clock: Clock): WeightedInspectionRollupManager {
    return WeightedInspectionRollupManager(
        store = store,
        validator = defaultWeightedInspectionRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
