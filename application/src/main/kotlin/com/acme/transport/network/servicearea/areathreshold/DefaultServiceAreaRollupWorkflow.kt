package com.acme.transport.network.servicearea.areathreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves default service area rollups through their life cycle and keeps a journal of what it did. */
data class DefaultServiceAreaRollupManager(
    var store: DefaultServiceAreaRollupStore,
    var validator: DefaultServiceAreaRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultServiceAreaRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default service area rollup $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultServiceAreaRollupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default service area rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultServiceAreaRollupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default service area rollup $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newDefaultServiceAreaRollupManager(store: DefaultServiceAreaRollupStore, clock: Clock, flags: Registry): DefaultServiceAreaRollupManager {
    return DefaultServiceAreaRollupManager(
        store = store,
        validator = defaultDefaultServiceAreaRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultServiceAreaRollupEngine(flags),
    )
}
