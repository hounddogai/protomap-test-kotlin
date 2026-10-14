package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break policies through their life cycle and keeps a journal of what it did. */
data class RestBreakPolicyManager(
    var store: RestBreakPolicyStore,
    var validator: RestBreakPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break policy $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakPolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakPolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rest break policy $code not found")
        val result = engine.evaluate(item)
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

fun newRestBreakPolicyManager(store: RestBreakPolicyStore, clock: Clock, flags: Registry): RestBreakPolicyManager {
    return RestBreakPolicyManager(
        store = store,
        validator = defaultRestBreakPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakPolicyEngine(flags),
    )
}
