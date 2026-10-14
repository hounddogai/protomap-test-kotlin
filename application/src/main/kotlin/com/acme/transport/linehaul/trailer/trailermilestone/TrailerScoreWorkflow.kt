package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer scores through their life cycle and keeps a journal of what it did. */
data class TrailerScoreWorkflow(
    var store: TrailerScoreStore,
    var validator: TrailerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer score $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerScoreStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerScoreStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTrailerScoreWorkflow(store: TrailerScoreStore, clock: Clock): TrailerScoreWorkflow {
    return TrailerScoreWorkflow(
        store = store,
        validator = defaultTrailerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
