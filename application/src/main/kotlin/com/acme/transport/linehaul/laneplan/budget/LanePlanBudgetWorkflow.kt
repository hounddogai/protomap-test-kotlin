package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan budgets through their life cycle and keeps a journal of what it did. */
data class LanePlanBudgetService(
    var store: LanePlanBudgetStore,
    var validator: LanePlanBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lane plan budget $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanBudgetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanBudgetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan budget $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLanePlanBudgetService(store: LanePlanBudgetStore, clock: Clock, flags: Registry): LanePlanBudgetService {
    return LanePlanBudgetService(
        store = store,
        validator = defaultLanePlanBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanBudgetEngine(flags),
    )
}
