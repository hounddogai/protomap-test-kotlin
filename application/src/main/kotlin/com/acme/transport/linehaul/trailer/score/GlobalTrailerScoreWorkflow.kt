package com.acme.transport.linehaul.trailer.score

import com.acme.transport.linehaul.clock.Clock

/** Moves global trailer scores through their life cycle and keeps a journal of what it did. */
data class GlobalTrailerScoreDesk(
    var store: GlobalTrailerScoreStore,
    var validator: GlobalTrailerScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global trailer score $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTrailerScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("global trailer score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTrailerScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newGlobalTrailerScoreDesk(store: GlobalTrailerScoreStore, clock: Clock): GlobalTrailerScoreDesk {
    return GlobalTrailerScoreDesk(
        store = store,
        validator = defaultGlobalTrailerScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
