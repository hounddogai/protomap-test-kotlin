package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.clock.Clock

/** Moves lease return rules through their life cycle and keeps a journal of what it did. */
data class LeaseReturnRuleDesk(
    var store: LeaseReturnRuleStore,
    var validator: LeaseReturnRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lease return rule $code not found")
        validator.requireValid(item)
        item.moveTo(LeaseReturnRuleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lease return rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LeaseReturnRuleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLeaseReturnRuleDesk(store: LeaseReturnRuleStore, clock: Clock): LeaseReturnRuleDesk {
    return LeaseReturnRuleDesk(
        store = store,
        validator = defaultLeaseReturnRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
