package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual duty period snapshots through their life cycle and keeps a journal of what it did. */
data class ManualDutyPeriodSnapshotManager(
    var store: ManualDutyPeriodSnapshotStore,
    var validator: ManualDutyPeriodSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDutyPeriodSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual duty period snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDutyPeriodSnapshotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual duty period snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDutyPeriodSnapshotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual duty period snapshot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newManualDutyPeriodSnapshotManager(store: ManualDutyPeriodSnapshotStore, clock: Clock, flags: Registry): ManualDutyPeriodSnapshotManager {
    return ManualDutyPeriodSnapshotManager(
        store = store,
        validator = defaultManualDutyPeriodSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDutyPeriodSnapshotEngine(flags),
    )
}
