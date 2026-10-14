package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.linehaul.clock.Clock

/** Moves capped trailer groups through their life cycle and keeps a journal of what it did. */
data class CappedTrailerGroupWorkflow(
    var store: CappedTrailerGroupStore,
    var validator: CappedTrailerGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped trailer group $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerGroupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerGroupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newCappedTrailerGroupWorkflow(store: CappedTrailerGroupStore, clock: Clock): CappedTrailerGroupWorkflow {
    return CappedTrailerGroupWorkflow(
        store = store,
        validator = defaultCappedTrailerGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
