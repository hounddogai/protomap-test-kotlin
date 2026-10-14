package com.acme.transport.fleetops.utilization.score

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization scores through their life cycle and keeps a journal of what it did. */
data class UtilizationScoreDesk(
    var store: UtilizationScoreStore,
    var validator: UtilizationScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("utilization score $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("utilization score $code not found")
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

fun newUtilizationScoreDesk(store: UtilizationScoreStore, clock: Clock): UtilizationScoreDesk {
    return UtilizationScoreDesk(
        store = store,
        validator = defaultUtilizationScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationScoreEngine(),
    )
}
