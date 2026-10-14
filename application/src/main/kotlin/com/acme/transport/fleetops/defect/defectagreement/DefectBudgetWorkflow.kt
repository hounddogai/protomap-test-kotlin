package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.fleetops.clock.Clock

/** Moves defect budgets through their life cycle and keeps a journal of what it did. */
data class DefectBudgetService(
    var store: DefectBudgetStore,
    var validator: DefectBudgetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefectBudgetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("defect budget $code not found")
        validator.requireValid(item)
        item.moveTo(DefectBudgetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("defect budget $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefectBudgetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("defect budget $code not found")
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

fun newDefectBudgetService(store: DefectBudgetStore, clock: Clock): DefectBudgetService {
    return DefectBudgetService(
        store = store,
        validator = defaultDefectBudgetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefectBudgetEngine(),
    )
}
