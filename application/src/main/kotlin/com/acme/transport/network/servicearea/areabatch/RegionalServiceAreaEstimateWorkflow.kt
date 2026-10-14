package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/**
 * Moves regional service area estimates through their life cycle and keeps a journal of what it did.
 */
data class RegionalServiceAreaEstimateCoordinator(
    var store: RegionalServiceAreaEstimateStore,
    var validator: RegionalServiceAreaEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalServiceAreaEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional service area estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalServiceAreaEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional service area estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalServiceAreaEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("regional service area estimate $code not found")
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

fun newRegionalServiceAreaEstimateCoordinator(store: RegionalServiceAreaEstimateStore, clock: Clock, flags: Registry): RegionalServiceAreaEstimateCoordinator {
    return RegionalServiceAreaEstimateCoordinator(
        store = store,
        validator = defaultRegionalServiceAreaEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalServiceAreaEstimateEngine(flags),
    )
}
