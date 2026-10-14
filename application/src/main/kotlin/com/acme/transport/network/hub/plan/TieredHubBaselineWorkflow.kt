package com.acme.transport.network.hub.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves tiered hub baselines through their life cycle and keeps a journal of what it did. */
data class TieredHubBaselineService(
    var store: TieredHubBaselineStore,
    var validator: TieredHubBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredHubBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered hub baseline $code not found")
        validator.requireValid(item)
        item.moveTo(TieredHubBaselineStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered hub baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredHubBaselineStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered hub baseline $code not found")
        val result = engine.compute(item)
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

fun newTieredHubBaselineService(store: TieredHubBaselineStore, clock: Clock, flags: Registry): TieredHubBaselineService {
    return TieredHubBaselineService(
        store = store,
        validator = defaultTieredHubBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredHubBaselineEngine(flags),
    )
}
