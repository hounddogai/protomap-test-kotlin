package com.acme.transport.freightpay.audit.policy

import com.acme.transport.freightpay.clock.Clock

/** Moves audit policies through their life cycle and keeps a journal of what it did. */
data class AuditPolicyCoordinator(
    var store: AuditPolicyStore,
    var validator: AuditPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("audit policy $code not found")
        validator.requireValid(item)
        item.moveTo(AuditPolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("audit policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditPolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("audit policy $code not found")
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

fun newAuditPolicyCoordinator(store: AuditPolicyStore, clock: Clock): AuditPolicyCoordinator {
    return AuditPolicyCoordinator(
        store = store,
        validator = defaultAuditPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditPolicyEngine(),
    )
}
