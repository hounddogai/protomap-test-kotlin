package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves global sortation milestones through their life cycle and keeps a journal of what it did. */
data class GlobalSortationMilestoneWorkflow(
    var store: GlobalSortationMilestoneStore,
    var validator: GlobalSortationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalSortationMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global sortation milestone $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalSortationMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global sortation milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalSortationMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global sortation milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalSortationMilestoneWorkflow(store: GlobalSortationMilestoneStore, clock: Clock, flags: Registry): GlobalSortationMilestoneWorkflow {
    return GlobalSortationMilestoneWorkflow(
        store = store,
        validator = defaultGlobalSortationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalSortationMilestoneEngine(flags),
    )
}
