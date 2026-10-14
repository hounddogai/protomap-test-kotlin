package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.linehaul.clock.Clock

/** Moves capped backhaul rules through their life cycle and keeps a journal of what it did. */
data class CappedBackhaulRuleWorkflow(
    var store: CappedBackhaulRuleStore,
    var validator: CappedBackhaulRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped backhaul rule $code not found")
        validator.requireValid(item)
        item.moveTo(CappedBackhaulRuleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capped backhaul rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedBackhaulRuleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newCappedBackhaulRuleWorkflow(store: CappedBackhaulRuleStore, clock: Clock): CappedBackhaulRuleWorkflow {
    return CappedBackhaulRuleWorkflow(
        store = store,
        validator = defaultCappedBackhaulRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
