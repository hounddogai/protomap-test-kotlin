package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.clock.Clock

/** Moves manual clearance overrides through their life cycle and keeps a journal of what it did. */
data class ManualClearanceOverrideDesk(
    var store: ManualClearanceOverrideStore,
    var validator: ManualClearanceOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual clearance override $code not found")
        validator.requireValid(item)
        item.moveTo(ManualClearanceOverrideStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual clearance override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualClearanceOverrideStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newManualClearanceOverrideDesk(store: ManualClearanceOverrideStore, clock: Clock): ManualClearanceOverrideDesk {
    return ManualClearanceOverrideDesk(
        store = store,
        validator = defaultManualClearanceOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
