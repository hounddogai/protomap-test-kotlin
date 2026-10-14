package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.clock.Clock

/** Moves broker milestones through their life cycle and keeps a journal of what it did. */
data class BrokerMilestoneManager(
    var store: BrokerMilestoneStore,
    var validator: BrokerMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker milestone $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerMilestoneStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("broker milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerMilestoneStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newBrokerMilestoneManager(store: BrokerMilestoneStore, clock: Clock): BrokerMilestoneManager {
    return BrokerMilestoneManager(
        store = store,
        validator = defaultBrokerMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
