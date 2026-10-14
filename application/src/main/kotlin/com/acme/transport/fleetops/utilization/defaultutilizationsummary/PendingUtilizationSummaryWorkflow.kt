package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending utilization summaries through their life cycle and keeps a journal of what it did. */
data class PendingUtilizationSummaryManager(
    var store: PendingUtilizationSummaryStore,
    var validator: PendingUtilizationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingUtilizationSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending utilization summary $code not found")
        validator.requireValid(item)
        item.moveTo(PendingUtilizationSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending utilization summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingUtilizationSummaryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending utilization summary $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingUtilizationSummaryManager(store: PendingUtilizationSummaryStore, clock: Clock, flags: Registry): PendingUtilizationSummaryManager {
    return PendingUtilizationSummaryManager(
        store = store,
        validator = defaultPendingUtilizationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingUtilizationSummaryEngine(flags),
    )
}
