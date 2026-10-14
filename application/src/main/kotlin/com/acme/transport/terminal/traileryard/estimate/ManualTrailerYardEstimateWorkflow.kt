package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.terminal.clock.Clock

/** Moves manual trailer yard estimates through their life cycle and keeps a journal of what it did. */
data class ManualTrailerYardEstimateDesk(
    var store: ManualTrailerYardEstimateStore,
    var validator: ManualTrailerYardEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual trailer yard estimate $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTrailerYardEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual trailer yard estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTrailerYardEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newManualTrailerYardEstimateDesk(store: ManualTrailerYardEstimateStore, clock: Clock): ManualTrailerYardEstimateDesk {
    return ManualTrailerYardEstimateDesk(
        store = store,
        validator = defaultManualTrailerYardEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
