package com.acme.transport.safety.violation.plan

import com.acme.transport.safety.clock.Clock

/** Moves violation policies through their life cycle and keeps a journal of what it did. */
data class ViolationPolicyService(
    var store: ViolationPolicyStore,
    var validator: ViolationPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation policy $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationPolicyStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationPolicyStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("violation policy $code not found")
        val result = engine.evaluate(item)
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

fun newViolationPolicyService(store: ViolationPolicyStore, clock: Clock): ViolationPolicyService {
    return ViolationPolicyService(
        store = store,
        validator = defaultViolationPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationPolicyEngine(),
    )
}
