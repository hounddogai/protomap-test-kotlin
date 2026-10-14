package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.clock.Clock

/** Moves global duty period rollups through their life cycle and keeps a journal of what it did. */
data class GlobalDutyPeriodRollupDesk(
    var store: GlobalDutyPeriodRollupStore,
    var validator: GlobalDutyPeriodRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global duty period rollup $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDutyPeriodRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global duty period rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDutyPeriodRollupStatus.FAILED)
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

fun newGlobalDutyPeriodRollupDesk(store: GlobalDutyPeriodRollupStore, clock: Clock): GlobalDutyPeriodRollupDesk {
    return GlobalDutyPeriodRollupDesk(
        store = store,
        validator = defaultGlobalDutyPeriodRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
