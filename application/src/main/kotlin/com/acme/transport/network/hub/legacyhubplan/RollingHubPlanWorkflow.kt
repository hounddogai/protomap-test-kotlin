package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.network.clock.Clock

/** Moves rolling hub plans through their life cycle and keeps a journal of what it did. */
data class RollingHubPlanService(
    var store: RollingHubPlanStore,
    var validator: RollingHubPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling hub plan $code not found")
        validator.requireValid(item)
        item.moveTo(RollingHubPlanStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling hub plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingHubPlanStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newRollingHubPlanService(store: RollingHubPlanStore, clock: Clock): RollingHubPlanService {
    return RollingHubPlanService(
        store = store,
        validator = defaultRollingHubPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
