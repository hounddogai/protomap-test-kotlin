package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.clock.Clock

/** Moves capped inspection batches through their life cycle and keeps a journal of what it did. */
data class CappedInspectionBatchManager(
    var store: CappedInspectionBatchStore,
    var validator: CappedInspectionBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedInspectionBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped inspection batch $code not found")
        validator.requireValid(item)
        item.moveTo(CappedInspectionBatchStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped inspection batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedInspectionBatchStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped inspection batch $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newCappedInspectionBatchManager(store: CappedInspectionBatchStore, clock: Clock): CappedInspectionBatchManager {
    return CappedInspectionBatchManager(
        store = store,
        validator = defaultCappedInspectionBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedInspectionBatchEngine(),
    )
}
