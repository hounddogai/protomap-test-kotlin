package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lease return summaries through their life cycle and keeps a journal of what it did. */
data class LeaseReturnSummaryCoordinator(
    var store: LeaseReturnSummaryStore,
    var validator: LeaseReturnSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LeaseReturnSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lease return summary $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnSummaryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnSummaryStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lease return summary $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLeaseReturnSummaryCoordinator(store: LeaseReturnSummaryStore, clock: Clock, flags: Registry): LeaseReturnSummaryCoordinator {
    return LeaseReturnSummaryCoordinator(
        store = store,
        validator = defaultLeaseReturnSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLeaseReturnSummaryEngine(flags),
    )
}
