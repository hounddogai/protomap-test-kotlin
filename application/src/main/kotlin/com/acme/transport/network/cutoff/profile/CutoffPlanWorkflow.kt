package com.acme.transport.network.cutoff.profile

import com.acme.transport.network.clock.Clock

/** Moves cutoff plans through their life cycle and keeps a journal of what it did. */
data class CutoffPlanService(
    var store: CutoffPlanStore,
    var validator: CutoffPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("cutoff plan $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffPlanStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffPlanStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("cutoff plan $code not found")
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

fun newCutoffPlanService(store: CutoffPlanStore, clock: Clock): CutoffPlanService {
    return CutoffPlanService(
        store = store,
        validator = defaultCutoffPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffPlanEngine(),
    )
}
