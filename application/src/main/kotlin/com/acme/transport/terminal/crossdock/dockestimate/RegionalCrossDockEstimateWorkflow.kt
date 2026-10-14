package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves regional cross dock estimates through their life cycle and keeps a journal of what it did. */
data class RegionalCrossDockEstimateService(
    var store: RegionalCrossDockEstimateStore,
    var validator: RegionalCrossDockEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCrossDockEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional cross dock estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCrossDockEstimateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional cross dock estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCrossDockEstimateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional cross dock estimate $code not found")
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

fun newRegionalCrossDockEstimateService(store: RegionalCrossDockEstimateStore, clock: Clock, flags: Registry): RegionalCrossDockEstimateService {
    return RegionalCrossDockEstimateService(
        store = store,
        validator = defaultRegionalCrossDockEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCrossDockEstimateEngine(flags),
    )
}
