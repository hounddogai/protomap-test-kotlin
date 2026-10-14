package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.lastmile.clock.Clock

/** Moves attempt adjustments through their life cycle and keeps a journal of what it did. */
data class AttemptAdjustmentDesk(
    var store: AttemptAdjustmentStore,
    var validator: AttemptAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("attempt adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newAttemptAdjustmentDesk(store: AttemptAdjustmentStore, clock: Clock): AttemptAdjustmentDesk {
    return AttemptAdjustmentDesk(
        store = store,
        validator = defaultAttemptAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
