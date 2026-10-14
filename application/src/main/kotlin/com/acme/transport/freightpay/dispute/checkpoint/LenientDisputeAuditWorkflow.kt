package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.Clock

/** Moves lenient dispute audits through their life cycle and keeps a journal of what it did. */
data class LenientDisputeAuditDesk(
    var store: LenientDisputeAuditStore,
    var validator: LenientDisputeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDisputeAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient dispute audit $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDisputeAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient dispute audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDisputeAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient dispute audit $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientDisputeAuditDesk(store: LenientDisputeAuditStore, clock: Clock): LenientDisputeAuditDesk {
    return LenientDisputeAuditDesk(
        store = store,
        validator = defaultLenientDisputeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDisputeAuditEngine(),
    )
}
