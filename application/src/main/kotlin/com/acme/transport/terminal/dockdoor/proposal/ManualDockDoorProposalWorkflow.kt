package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves manual dock door proposals through their life cycle and keeps a journal of what it did. */
data class ManualDockDoorProposalService(
    var store: ManualDockDoorProposalStore,
    var validator: ManualDockDoorProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDockDoorProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual dock door proposal $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDockDoorProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual dock door proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDockDoorProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual dock door proposal $code not found")
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

fun newManualDockDoorProposalService(store: ManualDockDoorProposalStore, clock: Clock, flags: Registry): ManualDockDoorProposalService {
    return ManualDockDoorProposalService(
        store = store,
        validator = defaultManualDockDoorProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDockDoorProposalEngine(flags),
    )
}
