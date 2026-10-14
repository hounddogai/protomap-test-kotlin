package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer variances through their life cycle and keeps a journal of what it did. */
data class TrailerVarianceDesk(
    var store: TrailerVarianceStore,
    var validator: TrailerVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer variance $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTrailerVarianceDesk(store: TrailerVarianceStore, clock: Clock): TrailerVarianceDesk {
    return TrailerVarianceDesk(
        store = store,
        validator = defaultTrailerVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
