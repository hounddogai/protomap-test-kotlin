package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves fallback violation slots through their life cycle and keeps a journal of what it did. */
data class FallbackViolationSlotWorkflow(
    var store: FallbackViolationSlotStore,
    var validator: FallbackViolationSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackViolationSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback violation slot $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackViolationSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback violation slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackViolationSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback violation slot $code not found")
        val result = engine.evaluate(item)
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

fun newFallbackViolationSlotWorkflow(store: FallbackViolationSlotStore, clock: Clock, flags: Registry): FallbackViolationSlotWorkflow {
    return FallbackViolationSlotWorkflow(
        store = store,
        validator = defaultFallbackViolationSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackViolationSlotEngine(flags),
    )
}
