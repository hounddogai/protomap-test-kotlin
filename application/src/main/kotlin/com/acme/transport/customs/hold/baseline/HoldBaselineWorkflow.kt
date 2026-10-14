package com.acme.transport.customs.hold.baseline

import com.acme.transport.customs.clock.Clock

/** Moves hold baselines through their life cycle and keeps a journal of what it did. */
data class HoldBaselineCoordinator(
    var store: HoldBaselineStore,
    var validator: HoldBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hold baseline $code not found")
        validator.requireValid(item)
        item.moveTo(HoldBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hold baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newHoldBaselineCoordinator(store: HoldBaselineStore, clock: Clock): HoldBaselineCoordinator {
    return HoldBaselineCoordinator(
        store = store,
        validator = defaultHoldBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
