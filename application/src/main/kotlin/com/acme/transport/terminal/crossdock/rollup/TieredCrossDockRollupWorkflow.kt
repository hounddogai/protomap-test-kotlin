package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.terminal.clock.Clock

/** Moves tiered cross dock rollups through their life cycle and keeps a journal of what it did. */
data class TieredCrossDockRollupCoordinator(
    var store: TieredCrossDockRollupStore,
    var validator: TieredCrossDockRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered cross dock rollup $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCrossDockRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cross dock rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCrossDockRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTieredCrossDockRollupCoordinator(store: TieredCrossDockRollupStore, clock: Clock): TieredCrossDockRollupCoordinator {
    return TieredCrossDockRollupCoordinator(
        store = store,
        validator = defaultTieredCrossDockRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
