package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves regional scorecard audits through their life cycle and keeps a journal of what it did. */
data class RegionalScorecardAuditWorkflow(
    var store: RegionalScorecardAuditStore,
    var validator: RegionalScorecardAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalScorecardAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional scorecard audit $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalScorecardAuditStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("regional scorecard audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalScorecardAuditStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional scorecard audit $code not found")
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

fun newRegionalScorecardAuditWorkflow(store: RegionalScorecardAuditStore, clock: Clock, flags: Registry): RegionalScorecardAuditWorkflow {
    return RegionalScorecardAuditWorkflow(
        store = store,
        validator = defaultRegionalScorecardAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalScorecardAuditEngine(flags),
    )
}
