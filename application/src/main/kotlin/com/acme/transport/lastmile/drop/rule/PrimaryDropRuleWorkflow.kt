package com.acme.transport.lastmile.drop.rule

import com.acme.transport.lastmile.clock.Clock

/** Moves primary drop rules through their life cycle and keeps a journal of what it did. */
data class PrimaryDropRuleDesk(
    var store: PrimaryDropRuleStore,
    var validator: PrimaryDropRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary drop rule $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDropRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary drop rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDropRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newPrimaryDropRuleDesk(store: PrimaryDropRuleStore, clock: Clock): PrimaryDropRuleDesk {
    return PrimaryDropRuleDesk(
        store = store,
        validator = defaultPrimaryDropRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
