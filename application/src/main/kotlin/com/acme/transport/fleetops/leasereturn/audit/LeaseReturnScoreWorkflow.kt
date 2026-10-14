package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return scores through their life cycle and keeps a journal of what it did. */
data class LeaseReturnScoreCoordinator(
    var store: LeaseReturnScoreStore,
    var validator: LeaseReturnScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lease return score $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lease return score $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLeaseReturnScoreCoordinator(store: LeaseReturnScoreStore, clock: Clock): LeaseReturnScoreCoordinator {
    return LeaseReturnScoreCoordinator(
        store = store,
        validator = defaultLeaseReturnScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnScoreEngine(),
    )
}
