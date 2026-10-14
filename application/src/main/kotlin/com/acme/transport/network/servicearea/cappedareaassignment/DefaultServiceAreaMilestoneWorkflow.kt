package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.network.clock.Clock

/**
 * Moves default service area milestones through their life cycle and keeps a journal of what it did.
 */
data class DefaultServiceAreaMilestoneDesk(
    var store: DefaultServiceAreaMilestoneStore,
    var validator: DefaultServiceAreaMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default service area milestone $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultServiceAreaMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default service area milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultServiceAreaMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newDefaultServiceAreaMilestoneDesk(store: DefaultServiceAreaMilestoneStore, clock: Clock): DefaultServiceAreaMilestoneDesk {
    return DefaultServiceAreaMilestoneDesk(
        store = store,
        validator = defaultDefaultServiceAreaMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
