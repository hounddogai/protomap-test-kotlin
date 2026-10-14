package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection estimates through their life cycle and keeps a journal of what it did. */
data class InspectionEstimateDesk(
    var store: InspectionEstimateStore,
    var validator: InspectionEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("inspection estimate $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionEstimateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionEstimateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("inspection estimate $code not found")
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
            if (item.createdAt > cutoff) {
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

fun newInspectionEstimateDesk(store: InspectionEstimateStore, clock: Clock, flags: Registry): InspectionEstimateDesk {
    return InspectionEstimateDesk(
        store = store,
        validator = defaultInspectionEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionEstimateEngine(flags),
    )
}
