package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.safety.clock.Clock

/** Moves scheduled near miss policies through their life cycle and keeps a journal of what it did. */
data class ScheduledNearMissPolicyWorkflow(
    var store: ScheduledNearMissPolicyStore,
    var validator: ScheduledNearMissPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled near miss policy $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledNearMissPolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled near miss policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledNearMissPolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newScheduledNearMissPolicyWorkflow(store: ScheduledNearMissPolicyStore, clock: Clock): ScheduledNearMissPolicyWorkflow {
    return ScheduledNearMissPolicyWorkflow(
        store = store,
        validator = defaultScheduledNearMissPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
