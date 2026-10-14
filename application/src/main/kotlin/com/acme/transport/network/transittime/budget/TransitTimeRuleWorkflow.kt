package com.acme.transport.network.transittime.budget

import com.acme.transport.network.clock.Clock

/** Moves transit time rules through their life cycle and keeps a journal of what it did. */
data class TransitTimeRuleDesk(
    var store: TransitTimeRuleStore,
    var validator: TransitTimeRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("transit time rule $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeRuleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeRuleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTransitTimeRuleDesk(store: TransitTimeRuleStore, clock: Clock): TransitTimeRuleDesk {
    return TransitTimeRuleDesk(
        store = store,
        validator = defaultTransitTimeRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
