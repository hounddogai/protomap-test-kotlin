package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.clock.Clock

/** Moves tiered duty period rollups through their life cycle and keeps a journal of what it did. */
data class TieredDutyPeriodRollupManager(
    var store: TieredDutyPeriodRollupStore,
    var validator: TieredDutyPeriodRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered duty period rollup $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDutyPeriodRollupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered duty period rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDutyPeriodRollupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTieredDutyPeriodRollupManager(store: TieredDutyPeriodRollupStore, clock: Clock): TieredDutyPeriodRollupManager {
    return TieredDutyPeriodRollupManager(
        store = store,
        validator = defaultTieredDutyPeriodRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
