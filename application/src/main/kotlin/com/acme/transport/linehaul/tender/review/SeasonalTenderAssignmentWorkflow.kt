package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal tender assignments through their life cycle and keeps a journal of what it did. */
data class SeasonalTenderAssignmentManager(
    var store: SeasonalTenderAssignmentStore,
    var validator: SeasonalTenderAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTenderAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal tender assignment $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTenderAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tender assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTenderAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal tender assignment $code not found")
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

fun newSeasonalTenderAssignmentManager(store: SeasonalTenderAssignmentStore, clock: Clock, flags: Registry): SeasonalTenderAssignmentManager {
    return SeasonalTenderAssignmentManager(
        store = store,
        validator = defaultSeasonalTenderAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTenderAssignmentEngine(flags),
    )
}
