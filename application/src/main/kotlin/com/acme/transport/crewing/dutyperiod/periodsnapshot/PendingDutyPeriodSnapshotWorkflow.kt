package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.crewing.clock.Clock

/** Moves pending duty period snapshots through their life cycle and keeps a journal of what it did. */
data class PendingDutyPeriodSnapshotDesk(
    var store: PendingDutyPeriodSnapshotStore,
    var validator: PendingDutyPeriodSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending duty period snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(PendingDutyPeriodSnapshotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending duty period snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingDutyPeriodSnapshotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPendingDutyPeriodSnapshotDesk(store: PendingDutyPeriodSnapshotStore, clock: Clock): PendingDutyPeriodSnapshotDesk {
    return PendingDutyPeriodSnapshotDesk(
        store = store,
        validator = defaultPendingDutyPeriodSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
