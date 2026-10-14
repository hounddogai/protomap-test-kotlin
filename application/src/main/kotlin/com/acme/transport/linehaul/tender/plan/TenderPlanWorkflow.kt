package com.acme.transport.linehaul.tender.plan

import com.acme.transport.linehaul.clock.Clock

/** Moves tender plans through their life cycle and keeps a journal of what it did. */
data class TenderPlanWorkflow(
    var store: TenderPlanStore,
    var validator: TenderPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tender plan $code not found")
        validator.requireValid(item)
        item.moveTo(TenderPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tender plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tender plan $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newTenderPlanWorkflow(store: TenderPlanStore, clock: Clock): TenderPlanWorkflow {
    return TenderPlanWorkflow(
        store = store,
        validator = defaultTenderPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderPlanEngine(),
    )
}
