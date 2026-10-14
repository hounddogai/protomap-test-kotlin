package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.clock.Clock

/** Moves duty period variances through their life cycle and keeps a journal of what it did. */
data class DutyPeriodVarianceWorkflow(
    var store: DutyPeriodVarianceStore,
    var validator: DutyPeriodVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("duty period variance $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("duty period variance $code not found")
        val result = engine.evaluate(item)
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

fun newDutyPeriodVarianceWorkflow(store: DutyPeriodVarianceStore, clock: Clock): DutyPeriodVarianceWorkflow {
    return DutyPeriodVarianceWorkflow(
        store = store,
        validator = defaultDutyPeriodVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodVarianceEngine(),
    )
}
