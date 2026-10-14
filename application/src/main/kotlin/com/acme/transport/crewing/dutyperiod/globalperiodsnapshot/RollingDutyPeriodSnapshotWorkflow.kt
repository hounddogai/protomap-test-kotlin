package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.crewing.clock.Clock

/** Moves rolling duty period snapshots through their life cycle and keeps a journal of what it did. */
data class RollingDutyPeriodSnapshotWorkflow(
    var store: RollingDutyPeriodSnapshotStore,
    var validator: RollingDutyPeriodSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDutyPeriodSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling duty period snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDutyPeriodSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling duty period snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDutyPeriodSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling duty period snapshot $code not found")
        val result = engine.evaluate(item)
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

fun newRollingDutyPeriodSnapshotWorkflow(store: RollingDutyPeriodSnapshotStore, clock: Clock): RollingDutyPeriodSnapshotWorkflow {
    return RollingDutyPeriodSnapshotWorkflow(
        store = store,
        validator = defaultRollingDutyPeriodSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDutyPeriodSnapshotEngine(),
    )
}
