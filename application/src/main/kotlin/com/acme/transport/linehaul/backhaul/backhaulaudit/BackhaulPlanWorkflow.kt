package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul plans through their life cycle and keeps a journal of what it did. */
data class BackhaulPlanWorkflow(
    var store: BackhaulPlanStore,
    var validator: BackhaulPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("backhaul plan $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("backhaul plan $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newBackhaulPlanWorkflow(store: BackhaulPlanStore, clock: Clock): BackhaulPlanWorkflow {
    return BackhaulPlanWorkflow(
        store = store,
        validator = defaultBackhaulPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulPlanEngine(),
    )
}
