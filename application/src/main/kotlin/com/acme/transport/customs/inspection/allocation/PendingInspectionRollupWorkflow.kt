package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending inspection rollups through their life cycle and keeps a journal of what it did. */
data class PendingInspectionRollupManager(
    var store: PendingInspectionRollupStore,
    var validator: PendingInspectionRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingInspectionRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending inspection rollup $code not found")
        validator.requireValid(item)
        item.moveTo(PendingInspectionRollupStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending inspection rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingInspectionRollupStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending inspection rollup $code not found")
        val result = engine.price(item)
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

fun newPendingInspectionRollupManager(store: PendingInspectionRollupStore, clock: Clock, flags: Registry): PendingInspectionRollupManager {
    return PendingInspectionRollupManager(
        store = store,
        validator = defaultPendingInspectionRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingInspectionRollupEngine(flags),
    )
}
