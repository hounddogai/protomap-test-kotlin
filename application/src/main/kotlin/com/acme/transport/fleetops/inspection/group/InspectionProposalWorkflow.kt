package com.acme.transport.fleetops.inspection.group

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection proposals through their life cycle and keeps a journal of what it did. */
data class InspectionProposalDesk(
    var store: InspectionProposalStore,
    var validator: InspectionProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection proposal $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection proposal $code not found")
        val result = engine.price(item)
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

fun newInspectionProposalDesk(store: InspectionProposalStore, clock: Clock, flags: Registry): InspectionProposalDesk {
    return InspectionProposalDesk(
        store = store,
        validator = defaultInspectionProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionProposalEngine(flags),
    )
}
