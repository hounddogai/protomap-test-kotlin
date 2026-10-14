package com.acme.transport.freightpay.audit.template

import com.acme.transport.freightpay.clock.Clock

/** Moves audit slots through their life cycle and keeps a journal of what it did. */
data class AuditSlotWorkflow(
    var store: AuditSlotStore,
    var validator: AuditSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("audit slot $code not found")
        validator.requireValid(item)
        item.moveTo(AuditSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("audit slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditSlotStatus.ARCHIVED)
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

fun newAuditSlotWorkflow(store: AuditSlotStore, clock: Clock): AuditSlotWorkflow {
    return AuditSlotWorkflow(
        store = store,
        validator = defaultAuditSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
