package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.clock.Clock

/** Moves violation baselines through their life cycle and keeps a journal of what it did. */
data class ViolationBaselineWorkflow(
    var store: ViolationBaselineStore,
    var validator: ViolationBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("violation baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("violation baseline $code not found")
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

fun newViolationBaselineWorkflow(store: ViolationBaselineStore, clock: Clock): ViolationBaselineWorkflow {
    return ViolationBaselineWorkflow(
        store = store,
        validator = defaultViolationBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationBaselineEngine(),
    )
}
