package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled redelivery rules through their life cycle and keeps a journal of what it did. */
data class ScheduledRedeliveryRuleWorkflow(
    var store: ScheduledRedeliveryRuleStore,
    var validator: ScheduledRedeliveryRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRedeliveryRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled redelivery rule $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRedeliveryRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled redelivery rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRedeliveryRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scheduled redelivery rule $code not found")
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

fun newScheduledRedeliveryRuleWorkflow(store: ScheduledRedeliveryRuleStore, clock: Clock): ScheduledRedeliveryRuleWorkflow {
    return ScheduledRedeliveryRuleWorkflow(
        store = store,
        validator = defaultScheduledRedeliveryRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRedeliveryRuleEngine(),
    )
}
