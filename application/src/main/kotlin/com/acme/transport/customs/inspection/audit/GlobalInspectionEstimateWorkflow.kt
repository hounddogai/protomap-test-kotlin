package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global inspection estimates through their life cycle and keeps a journal of what it did. */
data class GlobalInspectionEstimateCoordinator(
    var store: GlobalInspectionEstimateStore,
    var validator: GlobalInspectionEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalInspectionEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global inspection estimate $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalInspectionEstimateStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global inspection estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalInspectionEstimateStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global inspection estimate $code not found")
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

fun newGlobalInspectionEstimateCoordinator(store: GlobalInspectionEstimateStore, clock: Clock, flags: Registry): GlobalInspectionEstimateCoordinator {
    return GlobalInspectionEstimateCoordinator(
        store = store,
        validator = defaultGlobalInspectionEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalInspectionEstimateEngine(flags),
    )
}
