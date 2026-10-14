package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.linehaul.clock.Clock

/** Moves scheduled capacity proposals through their life cycle and keeps a journal of what it did. */
data class ScheduledCapacityProposalManager(
    var store: ScheduledCapacityProposalStore,
    var validator: ScheduledCapacityProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledCapacityProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled capacity proposal $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCapacityProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled capacity proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCapacityProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("scheduled capacity proposal $code not found")
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

fun newScheduledCapacityProposalManager(store: ScheduledCapacityProposalStore, clock: Clock): ScheduledCapacityProposalManager {
    return ScheduledCapacityProposalManager(
        store = store,
        validator = defaultScheduledCapacityProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledCapacityProposalEngine(),
    )
}
