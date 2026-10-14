package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.clock.Clock

/** Moves tire audits through their life cycle and keeps a journal of what it did. */
data class TireAuditCoordinator(
    var store: TireAuditStore,
    var validator: TireAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tire audit $code not found")
        validator.requireValid(item)
        item.moveTo(TireAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tire audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newTireAuditCoordinator(store: TireAuditStore, clock: Clock): TireAuditCoordinator {
    return TireAuditCoordinator(
        store = store,
        validator = defaultTireAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
