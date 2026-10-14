package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves inspection reviews through their life cycle and keeps a journal of what it did. */
data class InspectionReviewDesk(
    var store: InspectionReviewStore,
    var validator: InspectionReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("inspection review $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionReviewStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionReviewStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("inspection review $code not found")
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

fun newInspectionReviewDesk(store: InspectionReviewStore, clock: Clock, flags: Registry): InspectionReviewDesk {
    return InspectionReviewDesk(
        store = store,
        validator = defaultInspectionReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionReviewEngine(flags),
    )
}
