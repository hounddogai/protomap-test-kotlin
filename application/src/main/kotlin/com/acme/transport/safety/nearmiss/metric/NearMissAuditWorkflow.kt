package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.safety.clock.Clock

/** Moves near miss audits through their life cycle and keeps a journal of what it did. */
data class NearMissAuditService(
    var store: NearMissAuditStore,
    var validator: NearMissAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss audit $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newNearMissAuditService(store: NearMissAuditStore, clock: Clock): NearMissAuditService {
    return NearMissAuditService(
        store = store,
        validator = defaultNearMissAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
