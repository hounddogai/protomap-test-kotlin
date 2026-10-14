package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.clock.Clock

/**
 * Moves adaptive declaration milestones through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveDeclarationMilestoneService(
    var store: AdaptiveDeclarationMilestoneStore,
    var validator: AdaptiveDeclarationMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDeclarationMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive declaration milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDeclarationMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive declaration milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDeclarationMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive declaration milestone $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newAdaptiveDeclarationMilestoneService(store: AdaptiveDeclarationMilestoneStore, clock: Clock): AdaptiveDeclarationMilestoneService {
    return AdaptiveDeclarationMilestoneService(
        store = store,
        validator = defaultAdaptiveDeclarationMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDeclarationMilestoneEngine(),
    )
}
