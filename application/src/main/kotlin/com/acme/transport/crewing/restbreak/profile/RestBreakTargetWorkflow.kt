package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break targets through their life cycle and keeps a journal of what it did. */
data class RestBreakTargetWorkflow(
    var store: RestBreakTargetStore,
    var validator: RestBreakTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rest break target $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rest break target $code not found")
        val result = engine.run(item)
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

fun newRestBreakTargetWorkflow(store: RestBreakTargetStore, clock: Clock, flags: Registry): RestBreakTargetWorkflow {
    return RestBreakTargetWorkflow(
        store = store,
        validator = defaultRestBreakTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakTargetEngine(flags),
    )
}
