package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.clock.Clock

/** Moves rest break plans through their life cycle and keeps a journal of what it did. */
data class RestBreakPlanWorkflow(
    var store: RestBreakPlanStore,
    var validator: RestBreakPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rest break plan $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakPlanStatus.CANCELLED)
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

fun newRestBreakPlanWorkflow(store: RestBreakPlanStore, clock: Clock): RestBreakPlanWorkflow {
    return RestBreakPlanWorkflow(
        store = store,
        validator = defaultRestBreakPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
