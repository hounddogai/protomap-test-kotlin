package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute audits through their life cycle and keeps a journal of what it did. */
data class DisputeAuditDesk(
    var store: DisputeAuditStore,
    var validator: DisputeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dispute audit $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dispute audit $code not found")
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

fun newDisputeAuditDesk(store: DisputeAuditStore, clock: Clock): DisputeAuditDesk {
    return DisputeAuditDesk(
        store = store,
        validator = defaultDisputeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeAuditEngine(),
    )
}
