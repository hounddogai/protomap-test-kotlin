package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan overrides through their life cycle and keeps a journal of what it did. */
data class LanePlanOverrideDesk(
    var store: LanePlanOverrideStore,
    var validator: LanePlanOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lane plan override $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanOverrideStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanOverrideStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newLanePlanOverrideDesk(store: LanePlanOverrideStore, clock: Clock): LanePlanOverrideDesk {
    return LanePlanOverrideDesk(
        store = store,
        validator = defaultLanePlanOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
