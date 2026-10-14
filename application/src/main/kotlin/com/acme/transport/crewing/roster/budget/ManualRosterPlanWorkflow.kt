package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.clock.Clock

/** Moves manual roster plans through their life cycle and keeps a journal of what it did. */
data class ManualRosterPlanManager(
    var store: ManualRosterPlanStore,
    var validator: ManualRosterPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualRosterPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual roster plan $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRosterPlanStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual roster plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRosterPlanStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual roster plan $code not found")
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

fun newManualRosterPlanManager(store: ManualRosterPlanStore, clock: Clock): ManualRosterPlanManager {
    return ManualRosterPlanManager(
        store = store,
        validator = defaultManualRosterPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualRosterPlanEngine(),
    )
}
