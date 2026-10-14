package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.terminal.clock.Clock

/** Moves primary cross dock policies through their life cycle and keeps a journal of what it did. */
data class PrimaryCrossDockPolicyService(
    var store: PrimaryCrossDockPolicyStore,
    var validator: PrimaryCrossDockPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryCrossDockPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary cross dock policy $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCrossDockPolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary cross dock policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCrossDockPolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary cross dock policy $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newPrimaryCrossDockPolicyService(store: PrimaryCrossDockPolicyStore, clock: Clock): PrimaryCrossDockPolicyService {
    return PrimaryCrossDockPolicyService(
        store = store,
        validator = defaultPrimaryCrossDockPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryCrossDockPolicyEngine(),
    )
}
