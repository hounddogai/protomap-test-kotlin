package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.clock.Clock

/** Moves rest break estimates through their life cycle and keeps a journal of what it did. */
data class RestBreakEstimateWorkflow(
    var store: RestBreakEstimateStore,
    var validator: RestBreakEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rest break estimate $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakEstimateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rest break estimate $code not found")
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

fun newRestBreakEstimateWorkflow(store: RestBreakEstimateStore, clock: Clock): RestBreakEstimateWorkflow {
    return RestBreakEstimateWorkflow(
        store = store,
        validator = defaultRestBreakEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakEstimateEngine(),
    )
}
