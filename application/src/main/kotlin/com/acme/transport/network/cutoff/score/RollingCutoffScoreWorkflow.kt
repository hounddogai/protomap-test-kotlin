package com.acme.transport.network.cutoff.score

import com.acme.transport.network.clock.Clock

/** Moves rolling cutoff scores through their life cycle and keeps a journal of what it did. */
data class RollingCutoffScoreDesk(
    var store: RollingCutoffScoreStore,
    var validator: RollingCutoffScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling cutoff score $code not found")
        validator.requireValid(item)
        item.moveTo(RollingCutoffScoreStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling cutoff score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingCutoffScoreStatus.ARCHIVED)
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

fun newRollingCutoffScoreDesk(store: RollingCutoffScoreStore, clock: Clock): RollingCutoffScoreDesk {
    return RollingCutoffScoreDesk(
        store = store,
        validator = defaultRollingCutoffScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
