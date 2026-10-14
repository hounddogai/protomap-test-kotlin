package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.clock.Clock

/** Moves idling proposals through their life cycle and keeps a journal of what it did. */
data class IdlingProposalDesk(
    var store: IdlingProposalStore,
    var validator: IdlingProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("idling proposal $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingProposalStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingProposalStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling proposal $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newIdlingProposalDesk(store: IdlingProposalStore, clock: Clock): IdlingProposalDesk {
    return IdlingProposalDesk(
        store = store,
        validator = defaultIdlingProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingProposalEngine(),
    )
}
