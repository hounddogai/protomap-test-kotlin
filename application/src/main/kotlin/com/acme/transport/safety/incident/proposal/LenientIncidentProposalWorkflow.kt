package com.acme.transport.safety.incident.proposal

import com.acme.transport.safety.clock.Clock

/** Moves lenient incident proposals through their life cycle and keeps a journal of what it did. */
data class LenientIncidentProposalCoordinator(
    var store: LenientIncidentProposalStore,
    var validator: LenientIncidentProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientIncidentProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient incident proposal $code not found")
        validator.requireValid(item)
        item.moveTo(LenientIncidentProposalStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient incident proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientIncidentProposalStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient incident proposal $code not found")
        val result = engine.run(item)
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

fun newLenientIncidentProposalCoordinator(store: LenientIncidentProposalStore, clock: Clock): LenientIncidentProposalCoordinator {
    return LenientIncidentProposalCoordinator(
        store = store,
        validator = defaultLenientIncidentProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientIncidentProposalEngine(),
    )
}
