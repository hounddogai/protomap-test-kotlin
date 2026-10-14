package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves global trailer yard policies through their life cycle and keeps a journal of what it did. */
data class GlobalTrailerYardPolicyService(
    var store: GlobalTrailerYardPolicyStore,
    var validator: GlobalTrailerYardPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalTrailerYardPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global trailer yard policy $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTrailerYardPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global trailer yard policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTrailerYardPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global trailer yard policy $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newGlobalTrailerYardPolicyService(store: GlobalTrailerYardPolicyStore, clock: Clock, flags: Registry): GlobalTrailerYardPolicyService {
    return GlobalTrailerYardPolicyService(
        store = store,
        validator = defaultGlobalTrailerYardPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalTrailerYardPolicyEngine(flags),
    )
}
