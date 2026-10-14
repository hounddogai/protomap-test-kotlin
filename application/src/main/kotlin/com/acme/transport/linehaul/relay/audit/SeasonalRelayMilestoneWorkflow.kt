package com.acme.transport.linehaul.relay.audit

import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal relay milestones through their life cycle and keeps a journal of what it did. */
data class SeasonalRelayMilestoneWorkflow(
    var store: SeasonalRelayMilestoneStore,
    var validator: SeasonalRelayMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalRelayMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal relay milestone $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRelayMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal relay milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRelayMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal relay milestone $code not found")
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

fun newSeasonalRelayMilestoneWorkflow(store: SeasonalRelayMilestoneStore, clock: Clock): SeasonalRelayMilestoneWorkflow {
    return SeasonalRelayMilestoneWorkflow(
        store = store,
        validator = defaultSeasonalRelayMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalRelayMilestoneEngine(),
    )
}
