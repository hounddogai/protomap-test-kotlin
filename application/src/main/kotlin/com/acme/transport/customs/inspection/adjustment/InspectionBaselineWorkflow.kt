package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection baselines through their life cycle and keeps a journal of what it did. */
data class InspectionBaselineDesk(
    var store: InspectionBaselineStore,
    var validator: InspectionBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("inspection baseline $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionBaselineStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionBaselineStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("inspection baseline $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newInspectionBaselineDesk(store: InspectionBaselineStore, clock: Clock, flags: Registry): InspectionBaselineDesk {
    return InspectionBaselineDesk(
        store = store,
        validator = defaultInspectionBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionBaselineEngine(flags),
    )
}
