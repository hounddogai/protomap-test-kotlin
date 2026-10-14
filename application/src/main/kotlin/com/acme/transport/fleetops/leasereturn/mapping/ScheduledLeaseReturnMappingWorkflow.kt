package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves scheduled lease return mappings through their life cycle and keeps a journal of what it did.
 */
data class ScheduledLeaseReturnMappingManager(
    var store: ScheduledLeaseReturnMappingStore,
    var validator: ScheduledLeaseReturnMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled lease return mapping $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledLeaseReturnMappingStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled lease return mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledLeaseReturnMappingStatus.ARCHIVED)
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

fun newScheduledLeaseReturnMappingManager(store: ScheduledLeaseReturnMappingStore, clock: Clock): ScheduledLeaseReturnMappingManager {
    return ScheduledLeaseReturnMappingManager(
        store = store,
        validator = defaultScheduledLeaseReturnMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
