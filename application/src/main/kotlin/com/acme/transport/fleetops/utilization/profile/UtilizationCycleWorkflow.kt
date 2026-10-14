package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.clock.Clock

/** Moves utilization cycles through their life cycle and keeps a journal of what it did. */
data class UtilizationCycleWorkflow(
    var store: UtilizationCycleStore,
    var validator: UtilizationCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: UtilizationCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("utilization cycle $code not found")
        validator.requireValid(item)
        item.moveTo(UtilizationCycleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("utilization cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(UtilizationCycleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("utilization cycle $code not found")
        val result = engine.compute(item)
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

fun newUtilizationCycleWorkflow(store: UtilizationCycleStore, clock: Clock): UtilizationCycleWorkflow {
    return UtilizationCycleWorkflow(
        store = store,
        validator = defaultUtilizationCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultUtilizationCycleEngine(),
    )
}
