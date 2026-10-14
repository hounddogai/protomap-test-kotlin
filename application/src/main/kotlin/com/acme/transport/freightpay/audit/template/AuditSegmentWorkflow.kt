package com.acme.transport.freightpay.audit.template

import com.acme.transport.freightpay.clock.Clock

/** Moves audit segments through their life cycle and keeps a journal of what it did. */
data class AuditSegmentWorkflow(
    var store: AuditSegmentStore,
    var validator: AuditSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("audit segment $code not found")
        validator.requireValid(item)
        item.moveTo(AuditSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("audit segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("audit segment $code not found")
        val result = engine.run(item)
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

fun newAuditSegmentWorkflow(store: AuditSegmentStore, clock: Clock): AuditSegmentWorkflow {
    return AuditSegmentWorkflow(
        store = store,
        validator = defaultAuditSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditSegmentEngine(),
    )
}
