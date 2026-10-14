package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.clock.Clock

/** Moves hold rollups through their life cycle and keeps a journal of what it did. */
data class HoldRollupManager(
    var store: HoldRollupStore,
    var validator: HoldRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold rollup $code not found")
        validator.requireValid(item)
        item.moveTo(HoldRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("hold rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newHoldRollupManager(store: HoldRollupStore, clock: Clock): HoldRollupManager {
    return HoldRollupManager(
        store = store,
        validator = defaultHoldRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
