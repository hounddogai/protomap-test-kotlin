package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback tire rollups through their life cycle and keeps a journal of what it did. */
data class FallbackTireRollupManager(
    var store: FallbackTireRollupStore,
    var validator: FallbackTireRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackTireRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback tire rollup $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackTireRollupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback tire rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackTireRollupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback tire rollup $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newFallbackTireRollupManager(store: FallbackTireRollupStore, clock: Clock, flags: Registry): FallbackTireRollupManager {
    return FallbackTireRollupManager(
        store = store,
        validator = defaultFallbackTireRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackTireRollupEngine(flags),
    )
}
