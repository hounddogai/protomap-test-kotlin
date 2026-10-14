package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization targets through their life cycle and keeps a journal of what it did. */
data class UtilizationTargetService(
    var store: UtilizationTargetStore,
    var validator: UtilizationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("utilization target $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationTargetStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationTargetStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newUtilizationTargetService(store: UtilizationTargetStore, clock: Clock): UtilizationTargetService {
    return UtilizationTargetService(
        store = store,
        validator = defaultUtilizationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
