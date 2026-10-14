package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization entries through their life cycle and keeps a journal of what it did. */
data class UtilizationEntryWorkflow(
    var store: UtilizationEntryStore,
    var validator: UtilizationEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("utilization entry $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationEntryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationEntryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newUtilizationEntryWorkflow(store: UtilizationEntryStore, clock: Clock): UtilizationEntryWorkflow {
    return UtilizationEntryWorkflow(
        store = store,
        validator = defaultUtilizationEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
