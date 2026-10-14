package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.crewing.clock.Clock

/** Moves default rest break scores through their life cycle and keeps a journal of what it did. */
data class DefaultRestBreakScoreManager(
    var store: DefaultRestBreakScoreStore,
    var validator: DefaultRestBreakScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default rest break score $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRestBreakScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default rest break score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRestBreakScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultRestBreakScoreManager(store: DefaultRestBreakScoreStore, clock: Clock): DefaultRestBreakScoreManager {
    return DefaultRestBreakScoreManager(
        store = store,
        validator = defaultDefaultRestBreakScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
