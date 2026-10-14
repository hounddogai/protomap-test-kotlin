package com.acme.transport.network.transittime.score

import com.acme.transport.network.clock.Clock

/** Moves strict transit time scores through their life cycle and keeps a journal of what it did. */
data class StrictTransitTimeScoreService(
    var store: StrictTransitTimeScoreStore,
    var validator: StrictTransitTimeScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTransitTimeScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict transit time score $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTransitTimeScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict transit time score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTransitTimeScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict transit time score $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newStrictTransitTimeScoreService(store: StrictTransitTimeScoreStore, clock: Clock): StrictTransitTimeScoreService {
    return StrictTransitTimeScoreService(
        store = store,
        validator = defaultStrictTransitTimeScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTransitTimeScoreEngine(),
    )
}
