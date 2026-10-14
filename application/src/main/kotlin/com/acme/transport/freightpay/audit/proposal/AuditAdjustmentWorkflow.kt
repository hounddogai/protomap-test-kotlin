package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.clock.Clock

/** Moves audit adjustments through their life cycle and keeps a journal of what it did. */
data class AuditAdjustmentDesk(
    var store: AuditAdjustmentStore,
    var validator: AuditAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("audit adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(AuditAdjustmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("audit adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditAdjustmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newAuditAdjustmentDesk(store: AuditAdjustmentStore, clock: Clock): AuditAdjustmentDesk {
    return AuditAdjustmentDesk(
        store = store,
        validator = defaultAuditAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
