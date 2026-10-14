package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan scores through their life cycle and keeps a journal of what it did. */
data class LanePlanScoreWorkflow(
    var store: LanePlanScoreStore,
    var validator: LanePlanScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lane plan score $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newLanePlanScoreWorkflow(store: LanePlanScoreStore, clock: Clock): LanePlanScoreWorkflow {
    return LanePlanScoreWorkflow(
        store = store,
        validator = defaultLanePlanScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
