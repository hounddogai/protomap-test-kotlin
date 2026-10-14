package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted audit policies through their life cycle and keeps a journal of what it did. */
data class WeightedAuditPolicyCoordinator(
    var store: WeightedAuditPolicyStore,
    var validator: WeightedAuditPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAuditPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted audit policy $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAuditPolicyStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted audit policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAuditPolicyStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("weighted audit policy $code not found")
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

fun newWeightedAuditPolicyCoordinator(store: WeightedAuditPolicyStore, clock: Clock): WeightedAuditPolicyCoordinator {
    return WeightedAuditPolicyCoordinator(
        store = store,
        validator = defaultWeightedAuditPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAuditPolicyEngine(),
    )
}
