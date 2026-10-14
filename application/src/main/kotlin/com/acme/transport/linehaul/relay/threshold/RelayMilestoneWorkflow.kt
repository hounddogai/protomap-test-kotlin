package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.linehaul.clock.Clock

/** Moves relay milestones through their life cycle and keeps a journal of what it did. */
data class RelayMilestoneManager(
    var store: RelayMilestoneStore,
    var validator: RelayMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay milestone $code not found")
        validator.requireValid(item)
        item.moveTo(RelayMilestoneStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("relay milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayMilestoneStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("relay milestone $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRelayMilestoneManager(store: RelayMilestoneStore, clock: Clock): RelayMilestoneManager {
    return RelayMilestoneManager(
        store = store,
        validator = defaultRelayMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayMilestoneEngine(),
    )
}
