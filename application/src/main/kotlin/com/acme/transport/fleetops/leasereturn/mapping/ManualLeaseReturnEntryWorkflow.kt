package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual lease return entries through their life cycle and keeps a journal of what it did. */
data class ManualLeaseReturnEntryManager(
    var store: ManualLeaseReturnEntryStore,
    var validator: ManualLeaseReturnEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualLeaseReturnEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual lease return entry $code not found")
        validator.requireValid(item)
        item.moveTo(ManualLeaseReturnEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual lease return entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualLeaseReturnEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual lease return entry $code not found")
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

fun newManualLeaseReturnEntryManager(store: ManualLeaseReturnEntryStore, clock: Clock, flags: Registry): ManualLeaseReturnEntryManager {
    return ManualLeaseReturnEntryManager(
        store = store,
        validator = defaultManualLeaseReturnEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualLeaseReturnEntryEngine(flags),
    )
}
