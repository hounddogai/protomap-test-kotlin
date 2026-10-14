package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break templates through their life cycle and keeps a journal of what it did. */
data class RestBreakTemplateWorkflow(
    var store: RestBreakTemplateStore,
    var validator: RestBreakTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rest break template $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rest break template $code not found")
        val result = engine.evaluate(item)
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

fun newRestBreakTemplateWorkflow(store: RestBreakTemplateStore, clock: Clock, flags: Registry): RestBreakTemplateWorkflow {
    return RestBreakTemplateWorkflow(
        store = store,
        validator = defaultRestBreakTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakTemplateEngine(flags),
    )
}
