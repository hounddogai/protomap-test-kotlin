package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.fleetops.clock.Clock

/**
 * Moves fallback utilization summaries through their life cycle and keeps a journal of what it did.
 */
data class FallbackUtilizationSummaryDesk(
    var store: FallbackUtilizationSummaryStore,
    var validator: FallbackUtilizationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackUtilizationSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback utilization summary $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackUtilizationSummaryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback utilization summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackUtilizationSummaryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback utilization summary $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackUtilizationSummaryDesk(store: FallbackUtilizationSummaryStore, clock: Clock): FallbackUtilizationSummaryDesk {
    return FallbackUtilizationSummaryDesk(
        store = store,
        validator = defaultFallbackUtilizationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackUtilizationSummaryEngine(),
    )
}
