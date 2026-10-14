package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.clock.Clock

/** Moves manual inspection groups through their life cycle and keeps a journal of what it did. */
data class ManualInspectionGroupManager(
    var store: ManualInspectionGroupStore,
    var validator: ManualInspectionGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualInspectionGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual inspection group $code not found")
        validator.requireValid(item)
        item.moveTo(ManualInspectionGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual inspection group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualInspectionGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual inspection group $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newManualInspectionGroupManager(store: ManualInspectionGroupStore, clock: Clock): ManualInspectionGroupManager {
    return ManualInspectionGroupManager(
        store = store,
        validator = defaultManualInspectionGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualInspectionGroupEngine(),
    )
}
