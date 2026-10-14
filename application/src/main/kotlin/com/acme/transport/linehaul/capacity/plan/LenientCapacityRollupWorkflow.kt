package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient capacity rollups through their life cycle and keeps a journal of what it did. */
data class LenientCapacityRollupService(
    var store: LenientCapacityRollupStore,
    var validator: LenientCapacityRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient capacity rollup $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCapacityRollupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient capacity rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCapacityRollupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLenientCapacityRollupService(store: LenientCapacityRollupStore, clock: Clock): LenientCapacityRollupService {
    return LenientCapacityRollupService(
        store = store,
        validator = defaultLenientCapacityRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
