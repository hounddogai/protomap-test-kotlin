package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.network.clock.Clock

/** Moves scheduled service area rules through their life cycle and keeps a journal of what it did. */
data class ScheduledServiceAreaRuleCoordinator(
    var store: ScheduledServiceAreaRuleStore,
    var validator: ScheduledServiceAreaRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled service area rule $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledServiceAreaRuleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled service area rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledServiceAreaRuleStatus.CANCELLED)
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

fun newScheduledServiceAreaRuleCoordinator(store: ScheduledServiceAreaRuleStore, clock: Clock): ScheduledServiceAreaRuleCoordinator {
    return ScheduledServiceAreaRuleCoordinator(
        store = store,
        validator = defaultScheduledServiceAreaRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
