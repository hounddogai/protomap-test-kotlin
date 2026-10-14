package com.acme.transport.crewing.availability.preference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered availability rollups through their life cycle and keeps a journal of what it did. */
data class TieredAvailabilityRollupCoordinator(
    var store: TieredAvailabilityRollupStore,
    var validator: TieredAvailabilityRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredAvailabilityRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered availability rollup $code not found")
        validator.requireValid(item)
        item.moveTo(TieredAvailabilityRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered availability rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredAvailabilityRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered availability rollup $code not found")
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

fun newTieredAvailabilityRollupCoordinator(store: TieredAvailabilityRollupStore, clock: Clock, flags: Registry): TieredAvailabilityRollupCoordinator {
    return TieredAvailabilityRollupCoordinator(
        store = store,
        validator = defaultTieredAvailabilityRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredAvailabilityRollupEngine(flags),
    )
}
