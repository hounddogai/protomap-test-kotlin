package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization audits through their life cycle and keeps a journal of what it did. */
data class UtilizationAuditService(
    var store: UtilizationAuditStore,
    var validator: UtilizationAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("utilization audit $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationAuditStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationAuditStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newUtilizationAuditService(store: UtilizationAuditStore, clock: Clock): UtilizationAuditService {
    return UtilizationAuditService(
        store = store,
        validator = defaultUtilizationAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
