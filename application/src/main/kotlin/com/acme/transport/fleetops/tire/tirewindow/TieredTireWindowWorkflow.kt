package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.fleetops.clock.Clock

/** Moves tiered tire windows through their life cycle and keeps a journal of what it did. */
data class TieredTireWindowManager(
    var store: TieredTireWindowStore,
    var validator: TieredTireWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered tire window $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTireWindowStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered tire window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTireWindowStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newTieredTireWindowManager(store: TieredTireWindowStore, clock: Clock): TieredTireWindowManager {
    return TieredTireWindowManager(
        store = store,
        validator = defaultTieredTireWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
