package com.acme.transport.customs.clearance.claim

import com.acme.transport.customs.clock.Clock

/** Moves legacy clearance groups through their life cycle and keeps a journal of what it did. */
data class LegacyClearanceGroupDesk(
    var store: LegacyClearanceGroupStore,
    var validator: LegacyClearanceGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy clearance group $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyClearanceGroupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy clearance group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyClearanceGroupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLegacyClearanceGroupDesk(store: LegacyClearanceGroupStore, clock: Clock): LegacyClearanceGroupDesk {
    return LegacyClearanceGroupDesk(
        store = store,
        validator = defaultLegacyClearanceGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
