package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.fleetops.clock.Clock

/** Moves capped lease return schedules through their life cycle and keeps a journal of what it did. */
data class CappedLeaseReturnScheduleService(
    var store: CappedLeaseReturnScheduleStore,
    var validator: CappedLeaseReturnScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped lease return schedule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedLeaseReturnScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped lease return schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedLeaseReturnScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newCappedLeaseReturnScheduleService(store: CappedLeaseReturnScheduleStore, clock: Clock): CappedLeaseReturnScheduleService {
    return CappedLeaseReturnScheduleService(
        store = store,
        validator = defaultCappedLeaseReturnScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
