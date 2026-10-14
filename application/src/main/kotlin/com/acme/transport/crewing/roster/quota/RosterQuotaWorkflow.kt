package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.clock.Clock

/** Moves roster quotas through their life cycle and keeps a journal of what it did. */
data class RosterQuotaDesk(
    var store: RosterQuotaStore,
    var validator: RosterQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("roster quota $code not found")
        validator.requireValid(item)
        item.moveTo(RosterQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("roster quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RosterQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRosterQuotaDesk(store: RosterQuotaStore, clock: Clock): RosterQuotaDesk {
    return RosterQuotaDesk(
        store = store,
        validator = defaultRosterQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
