package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.clock.Clock

/** Moves inspection entries through their life cycle and keeps a journal of what it did. */
data class InspectionEntryWorkflow(
    var store: InspectionEntryStore,
    var validator: InspectionEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection entry $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionEntryStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionEntryStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection entry $code not found")
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

fun newInspectionEntryWorkflow(store: InspectionEntryStore, clock: Clock): InspectionEntryWorkflow {
    return InspectionEntryWorkflow(
        store = store,
        validator = defaultInspectionEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionEntryEngine(),
    )
}
