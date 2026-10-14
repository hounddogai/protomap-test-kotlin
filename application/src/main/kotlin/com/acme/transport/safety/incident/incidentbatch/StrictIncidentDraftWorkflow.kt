package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves strict incident drafts through their life cycle and keeps a journal of what it did. */
data class StrictIncidentDraftCoordinator(
    var store: StrictIncidentDraftStore,
    var validator: StrictIncidentDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictIncidentDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict incident draft $code not found")
        validator.requireValid(item)
        item.moveTo(StrictIncidentDraftStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict incident draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictIncidentDraftStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict incident draft $code not found")
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

fun newStrictIncidentDraftCoordinator(store: StrictIncidentDraftStore, clock: Clock, flags: Registry): StrictIncidentDraftCoordinator {
    return StrictIncidentDraftCoordinator(
        store = store,
        validator = defaultStrictIncidentDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictIncidentDraftEngine(flags),
    )
}
