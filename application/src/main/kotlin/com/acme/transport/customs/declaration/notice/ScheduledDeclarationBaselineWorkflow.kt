package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves scheduled declaration baselines through their life cycle and keeps a journal of what it did.
 */
data class ScheduledDeclarationBaselineWorkflow(
    var store: ScheduledDeclarationBaselineStore,
    var validator: ScheduledDeclarationBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledDeclarationBaselineEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled declaration baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDeclarationBaselineStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled declaration baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDeclarationBaselineStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("scheduled declaration baseline $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newScheduledDeclarationBaselineWorkflow(store: ScheduledDeclarationBaselineStore, clock: Clock, flags: Registry): ScheduledDeclarationBaselineWorkflow {
    return ScheduledDeclarationBaselineWorkflow(
        store = store,
        validator = defaultScheduledDeclarationBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledDeclarationBaselineEngine(flags),
    )
}
