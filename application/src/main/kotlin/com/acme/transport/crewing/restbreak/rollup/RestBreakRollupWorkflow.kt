package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break rollups through their life cycle and keeps a journal of what it did. */
data class RestBreakRollupCoordinator(
    var store: RestBreakRollupStore,
    var validator: RestBreakRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rest break rollup $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rest break rollup $code not found")
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

fun newRestBreakRollupCoordinator(store: RestBreakRollupStore, clock: Clock, flags: Registry): RestBreakRollupCoordinator {
    return RestBreakRollupCoordinator(
        store = store,
        validator = defaultRestBreakRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakRollupEngine(flags),
    )
}
