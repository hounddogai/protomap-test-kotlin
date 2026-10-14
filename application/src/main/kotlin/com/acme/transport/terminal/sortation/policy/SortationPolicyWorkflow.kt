package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation policies through their life cycle and keeps a journal of what it did. */
data class SortationPolicyManager(
    var store: SortationPolicyStore,
    var validator: SortationPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("sortation policy $code not found")
        validator.requireValid(item)
        item.moveTo(SortationPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("sortation policy $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newSortationPolicyManager(store: SortationPolicyStore, clock: Clock, flags: Registry): SortationPolicyManager {
    return SortationPolicyManager(
        store = store,
        validator = defaultSortationPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationPolicyEngine(flags),
    )
}
