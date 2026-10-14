package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.emissions.clock.Clock

/** Moves global idling scores through their life cycle and keeps a journal of what it did. */
data class GlobalIdlingScoreCoordinator(
    var store: GlobalIdlingScoreStore,
    var validator: GlobalIdlingScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global idling score $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalIdlingScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global idling score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalIdlingScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newGlobalIdlingScoreCoordinator(store: GlobalIdlingScoreStore, clock: Clock): GlobalIdlingScoreCoordinator {
    return GlobalIdlingScoreCoordinator(
        store = store,
        validator = defaultGlobalIdlingScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
