package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves global cutoff policies through their life cycle and keeps a journal of what it did. */
data class GlobalCutoffPolicyDesk(
    var store: GlobalCutoffPolicyStore,
    var validator: GlobalCutoffPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCutoffPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global cutoff policy $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCutoffPolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global cutoff policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCutoffPolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global cutoff policy $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newGlobalCutoffPolicyDesk(store: GlobalCutoffPolicyStore, clock: Clock, flags: Registry): GlobalCutoffPolicyDesk {
    return GlobalCutoffPolicyDesk(
        store = store,
        validator = defaultGlobalCutoffPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCutoffPolicyEngine(flags),
    )
}
