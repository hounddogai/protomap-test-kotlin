package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves attempt rules through their life cycle and keeps a journal of what it did. */
data class AttemptRuleWorkflow(
    var store: AttemptRuleStore,
    var validator: AttemptRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AttemptRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("attempt rule $code not found")
        validator.requireValid(item)
        item.moveTo(AttemptRuleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("attempt rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AttemptRuleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("attempt rule $code not found")
        val result = engine.price(item)
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

fun newAttemptRuleWorkflow(store: AttemptRuleStore, clock: Clock, flags: Registry): AttemptRuleWorkflow {
    return AttemptRuleWorkflow(
        store = store,
        validator = defaultAttemptRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAttemptRuleEngine(flags),
    )
}
