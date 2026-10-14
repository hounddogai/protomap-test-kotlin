package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.linehaul.clock.Clock

/** Moves pending relay rules through their life cycle and keeps a journal of what it did. */
data class PendingRelayRuleManager(
    var store: PendingRelayRuleStore,
    var validator: PendingRelayRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending relay rule $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRelayRuleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("pending relay rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRelayRuleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newPendingRelayRuleManager(store: PendingRelayRuleStore, clock: Clock): PendingRelayRuleManager {
    return PendingRelayRuleManager(
        store = store,
        validator = defaultPendingRelayRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
