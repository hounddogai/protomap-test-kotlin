package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves inspection groups through their life cycle and keeps a journal of what it did. */
data class InspectionGroupManager(
    var store: InspectionGroupStore,
    var validator: InspectionGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection group $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("inspection group $code not found")
        val result = engine.compute(item)
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

fun newInspectionGroupManager(store: InspectionGroupStore, clock: Clock): InspectionGroupManager {
    return InspectionGroupManager(
        store = store,
        validator = defaultInspectionGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionGroupEngine(),
    )
}
