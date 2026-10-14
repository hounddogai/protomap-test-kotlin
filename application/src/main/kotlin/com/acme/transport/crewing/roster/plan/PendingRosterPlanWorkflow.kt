package com.acme.transport.crewing.roster.plan

import com.acme.transport.crewing.clock.Clock

/** Moves pending roster plans through their life cycle and keeps a journal of what it did. */
data class PendingRosterPlanCoordinator(
    var store: PendingRosterPlanStore,
    var validator: PendingRosterPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending roster plan $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRosterPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending roster plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRosterPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newPendingRosterPlanCoordinator(store: PendingRosterPlanStore, clock: Clock): PendingRosterPlanCoordinator {
    return PendingRosterPlanCoordinator(
        store = store,
        validator = defaultPendingRosterPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
