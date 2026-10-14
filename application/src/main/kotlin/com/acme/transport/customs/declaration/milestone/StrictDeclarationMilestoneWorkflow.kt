package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.clock.Clock

/** Moves strict declaration milestones through their life cycle and keeps a journal of what it did. */
data class StrictDeclarationMilestoneWorkflow(
    var store: StrictDeclarationMilestoneStore,
    var validator: StrictDeclarationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict declaration milestone $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDeclarationMilestoneStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict declaration milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDeclarationMilestoneStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newStrictDeclarationMilestoneWorkflow(store: StrictDeclarationMilestoneStore, clock: Clock): StrictDeclarationMilestoneWorkflow {
    return StrictDeclarationMilestoneWorkflow(
        store = store,
        validator = defaultStrictDeclarationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
