package com.acme.transport.safety.violation.segment

import com.acme.transport.safety.clock.Clock

/** Moves violation audits through their life cycle and keeps a journal of what it did. */
data class ViolationAuditDesk(
    var store: ViolationAuditStore,
    var validator: ViolationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation audit $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("violation audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newViolationAuditDesk(store: ViolationAuditStore, clock: Clock): ViolationAuditDesk {
    return ViolationAuditDesk(
        store = store,
        validator = defaultViolationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
