package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.terminal.clock.Clock

/**
 * Moves pending trailer yard adjustments through their life cycle and keeps a journal of what it did.
 */
data class PendingTrailerYardAdjustmentWorkflow(
    var store: PendingTrailerYardAdjustmentStore,
    var validator: PendingTrailerYardAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTrailerYardAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending trailer yard adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTrailerYardAdjustmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending trailer yard adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTrailerYardAdjustmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("pending trailer yard adjustment $code not found")
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

fun newPendingTrailerYardAdjustmentWorkflow(store: PendingTrailerYardAdjustmentStore, clock: Clock): PendingTrailerYardAdjustmentWorkflow {
    return PendingTrailerYardAdjustmentWorkflow(
        store = store,
        validator = defaultPendingTrailerYardAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTrailerYardAdjustmentEngine(),
    )
}
