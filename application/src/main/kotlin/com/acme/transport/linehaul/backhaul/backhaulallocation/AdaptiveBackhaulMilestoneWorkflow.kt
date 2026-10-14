package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive backhaul milestones through their life cycle and keeps a journal of what it did. */
data class AdaptiveBackhaulMilestoneWorkflow(
    var store: AdaptiveBackhaulMilestoneStore,
    var validator: AdaptiveBackhaulMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveBackhaulMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive backhaul milestone $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveBackhaulMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive backhaul milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveBackhaulMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive backhaul milestone $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newAdaptiveBackhaulMilestoneWorkflow(store: AdaptiveBackhaulMilestoneStore, clock: Clock): AdaptiveBackhaulMilestoneWorkflow {
    return AdaptiveBackhaulMilestoneWorkflow(
        store = store,
        validator = defaultAdaptiveBackhaulMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveBackhaulMilestoneEngine(),
    )
}
