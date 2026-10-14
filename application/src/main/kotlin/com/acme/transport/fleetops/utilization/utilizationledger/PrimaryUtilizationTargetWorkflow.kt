package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.clock.Clock

/** Moves primary utilization targets through their life cycle and keeps a journal of what it did. */
data class PrimaryUtilizationTargetWorkflow(
    var store: PrimaryUtilizationTargetStore,
    var validator: PrimaryUtilizationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary utilization target $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryUtilizationTargetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary utilization target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryUtilizationTargetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newPrimaryUtilizationTargetWorkflow(store: PrimaryUtilizationTargetStore, clock: Clock): PrimaryUtilizationTargetWorkflow {
    return PrimaryUtilizationTargetWorkflow(
        store = store,
        validator = defaultPrimaryUtilizationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
