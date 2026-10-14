package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.fleetops.clock.Clock

/** Moves regional lease return audits through their life cycle and keeps a journal of what it did. */
data class RegionalLeaseReturnAuditManager(
    var store: RegionalLeaseReturnAuditStore,
    var validator: RegionalLeaseReturnAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalLeaseReturnAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional lease return audit $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalLeaseReturnAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional lease return audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalLeaseReturnAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional lease return audit $code not found")
        val result = engine.evaluate(item)
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

fun newRegionalLeaseReturnAuditManager(store: RegionalLeaseReturnAuditStore, clock: Clock): RegionalLeaseReturnAuditManager {
    return RegionalLeaseReturnAuditManager(
        store = store,
        validator = defaultRegionalLeaseReturnAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalLeaseReturnAuditEngine(),
    )
}
