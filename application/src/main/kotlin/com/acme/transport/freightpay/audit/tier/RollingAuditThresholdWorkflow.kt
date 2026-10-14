package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.clock.Clock

/** Moves rolling audit thresholds through their life cycle and keeps a journal of what it did. */
data class RollingAuditThresholdDesk(
    var store: RollingAuditThresholdStore,
    var validator: RollingAuditThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling audit threshold $code not found")
        validator.requireValid(item)
        item.moveTo(RollingAuditThresholdStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling audit threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingAuditThresholdStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newRollingAuditThresholdDesk(store: RollingAuditThresholdStore, clock: Clock): RollingAuditThresholdDesk {
    return RollingAuditThresholdDesk(
        store = store,
        validator = defaultRollingAuditThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
