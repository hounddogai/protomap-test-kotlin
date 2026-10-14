package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.linehaul.clock.Clock

/** Moves relay proposals through their life cycle and keeps a journal of what it did. */
data class RelayProposalCoordinator(
    var store: RelayProposalStore,
    var validator: RelayProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("relay proposal $code not found")
        validator.requireValid(item)
        item.moveTo(RelayProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("relay proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("relay proposal $code not found")
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

fun newRelayProposalCoordinator(store: RelayProposalStore, clock: Clock): RelayProposalCoordinator {
    return RelayProposalCoordinator(
        store = store,
        validator = defaultRelayProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayProposalEngine(),
    )
}
