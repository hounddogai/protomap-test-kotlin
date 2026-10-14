package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default rest break variances through their life cycle and keeps a journal of what it did. */
data class DefaultRestBreakVarianceWorkflow(
    var store: DefaultRestBreakVarianceStore,
    var validator: DefaultRestBreakVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultRestBreakVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default rest break variance $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRestBreakVarianceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default rest break variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRestBreakVarianceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default rest break variance $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newDefaultRestBreakVarianceWorkflow(store: DefaultRestBreakVarianceStore, clock: Clock, flags: Registry): DefaultRestBreakVarianceWorkflow {
    return DefaultRestBreakVarianceWorkflow(
        store = store,
        validator = defaultDefaultRestBreakVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultRestBreakVarianceEngine(flags),
    )
}
