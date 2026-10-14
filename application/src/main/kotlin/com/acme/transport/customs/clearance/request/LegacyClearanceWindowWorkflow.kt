package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.clock.Clock

/** Moves legacy clearance windows through their life cycle and keeps a journal of what it did. */
data class LegacyClearanceWindowManager(
    var store: LegacyClearanceWindowStore,
    var validator: LegacyClearanceWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy clearance window $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyClearanceWindowStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy clearance window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyClearanceWindowStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newLegacyClearanceWindowManager(store: LegacyClearanceWindowStore, clock: Clock): LegacyClearanceWindowManager {
    return LegacyClearanceWindowManager(
        store = store,
        validator = defaultLegacyClearanceWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
