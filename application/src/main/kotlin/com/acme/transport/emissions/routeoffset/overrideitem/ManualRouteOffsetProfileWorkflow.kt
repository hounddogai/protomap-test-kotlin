package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.emissions.clock.Clock

/** Moves manual route offset profiles through their life cycle and keeps a journal of what it did. */
data class ManualRouteOffsetProfileWorkflow(
    var store: ManualRouteOffsetProfileStore,
    var validator: ManualRouteOffsetProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual route offset profile $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRouteOffsetProfileStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual route offset profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRouteOffsetProfileStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newManualRouteOffsetProfileWorkflow(store: ManualRouteOffsetProfileStore, clock: Clock): ManualRouteOffsetProfileWorkflow {
    return ManualRouteOffsetProfileWorkflow(
        store = store,
        validator = defaultManualRouteOffsetProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
