package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy drop slots through their life cycle and keeps a journal of what it did. */
data class LegacyDropSlotManager(
    var store: LegacyDropSlotStore,
    var validator: LegacyDropSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy drop slot $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDropSlotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy drop slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDropSlotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLegacyDropSlotManager(store: LegacyDropSlotStore, clock: Clock): LegacyDropSlotManager {
    return LegacyDropSlotManager(
        store = store,
        validator = defaultLegacyDropSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
