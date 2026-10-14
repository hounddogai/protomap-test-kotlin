package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.lastmile.clock.Clock

/** Moves legacy attempt audits through their life cycle and keeps a journal of what it did. */
data class LegacyAttemptAuditDesk(
    var store: LegacyAttemptAuditStore,
    var validator: LegacyAttemptAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy attempt audit $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAttemptAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy attempt audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAttemptAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newLegacyAttemptAuditDesk(store: LegacyAttemptAuditStore, clock: Clock): LegacyAttemptAuditDesk {
    return LegacyAttemptAuditDesk(
        store = store,
        validator = defaultLegacyAttemptAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
