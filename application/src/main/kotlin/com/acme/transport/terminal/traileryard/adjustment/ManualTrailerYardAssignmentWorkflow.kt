package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves manual trailer yard assignments through their life cycle and keeps a journal of what it did.
 */
data class ManualTrailerYardAssignmentCoordinator(
    var store: ManualTrailerYardAssignmentStore,
    var validator: ManualTrailerYardAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTrailerYardAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual trailer yard assignment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTrailerYardAssignmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual trailer yard assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTrailerYardAssignmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual trailer yard assignment $code not found")
        val result = engine.price(item)
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

fun newManualTrailerYardAssignmentCoordinator(store: ManualTrailerYardAssignmentStore, clock: Clock, flags: Registry): ManualTrailerYardAssignmentCoordinator {
    return ManualTrailerYardAssignmentCoordinator(
        store = store,
        validator = defaultManualTrailerYardAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTrailerYardAssignmentEngine(flags),
    )
}
