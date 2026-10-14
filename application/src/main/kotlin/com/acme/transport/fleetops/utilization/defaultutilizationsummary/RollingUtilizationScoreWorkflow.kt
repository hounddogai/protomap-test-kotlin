package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.clock.Clock

/** Moves rolling utilization scores through their life cycle and keeps a journal of what it did. */
data class RollingUtilizationScoreService(
    var store: RollingUtilizationScoreStore,
    var validator: RollingUtilizationScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling utilization score $code not found")
        validator.requireValid(item)
        item.moveTo(RollingUtilizationScoreStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling utilization score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingUtilizationScoreStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRollingUtilizationScoreService(store: RollingUtilizationScoreStore, clock: Clock): RollingUtilizationScoreService {
    return RollingUtilizationScoreService(
        store = store,
        validator = defaultRollingUtilizationScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
