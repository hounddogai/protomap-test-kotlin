package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.clock.Clock

/** Moves rest break scores through their life cycle and keeps a journal of what it did. */
data class RestBreakScoreManager(
    var store: RestBreakScoreStore,
    var validator: RestBreakScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break score $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakScoreStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakScoreStatus.FAILED)
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

fun newRestBreakScoreManager(store: RestBreakScoreStore, clock: Clock): RestBreakScoreManager {
    return RestBreakScoreManager(
        store = store,
        validator = defaultRestBreakScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
