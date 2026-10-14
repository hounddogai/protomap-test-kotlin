package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves tire scores through their life cycle and keeps a journal of what it did. */
data class TireScoreCoordinator(
    var store: TireScoreStore,
    var validator: TireScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tire score $code not found")
        validator.requireValid(item)
        item.moveTo(TireScoreStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tire score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireScoreStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tire score $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newTireScoreCoordinator(store: TireScoreStore, clock: Clock): TireScoreCoordinator {
    return TireScoreCoordinator(
        store = store,
        validator = defaultTireScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireScoreEngine(),
    )
}
