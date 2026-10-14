package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.linehaul.clock.Clock

/** Moves tiered tender rollups through their life cycle and keeps a journal of what it did. */
data class TieredTenderRollupManager(
    var store: TieredTenderRollupStore,
    var validator: TieredTenderRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered tender rollup $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTenderRollupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered tender rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTenderRollupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newTieredTenderRollupManager(store: TieredTenderRollupStore, clock: Clock): TieredTenderRollupManager {
    return TieredTenderRollupManager(
        store = store,
        validator = defaultTieredTenderRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
