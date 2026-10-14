package com.acme.transport.linehaul.tender.quota

import com.acme.transport.linehaul.clock.Clock

/** Moves tender segments through their life cycle and keeps a journal of what it did. */
data class TenderSegmentWorkflow(
    var store: TenderSegmentStore,
    var validator: TenderSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tender segment $code not found")
        validator.requireValid(item)
        item.moveTo(TenderSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tender segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tender segment $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newTenderSegmentWorkflow(store: TenderSegmentStore, clock: Clock): TenderSegmentWorkflow {
    return TenderSegmentWorkflow(
        store = store,
        validator = defaultTenderSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderSegmentEngine(),
    )
}
