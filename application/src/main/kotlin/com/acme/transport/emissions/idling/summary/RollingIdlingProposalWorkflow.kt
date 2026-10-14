package com.acme.transport.emissions.idling.summary

import com.acme.transport.emissions.clock.Clock

/** Moves rolling idling proposals through their life cycle and keeps a journal of what it did. */
data class RollingIdlingProposalDesk(
    var store: RollingIdlingProposalStore,
    var validator: RollingIdlingProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingIdlingProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling idling proposal $code not found")
        validator.requireValid(item)
        item.moveTo(RollingIdlingProposalStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling idling proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingIdlingProposalStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling idling proposal $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRollingIdlingProposalDesk(store: RollingIdlingProposalStore, clock: Clock): RollingIdlingProposalDesk {
    return RollingIdlingProposalDesk(
        store = store,
        validator = defaultRollingIdlingProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingIdlingProposalEngine(),
    )
}
