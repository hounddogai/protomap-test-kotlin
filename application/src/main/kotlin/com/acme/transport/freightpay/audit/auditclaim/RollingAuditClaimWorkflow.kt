package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.freightpay.clock.Clock

/** Moves rolling audit claims through their life cycle and keeps a journal of what it did. */
data class RollingAuditClaimService(
    var store: RollingAuditClaimStore,
    var validator: RollingAuditClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingAuditClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling audit claim $code not found")
        validator.requireValid(item)
        item.moveTo(RollingAuditClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling audit claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingAuditClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling audit claim $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newRollingAuditClaimService(store: RollingAuditClaimStore, clock: Clock): RollingAuditClaimService {
    return RollingAuditClaimService(
        store = store,
        validator = defaultRollingAuditClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingAuditClaimEngine(),
    )
}
