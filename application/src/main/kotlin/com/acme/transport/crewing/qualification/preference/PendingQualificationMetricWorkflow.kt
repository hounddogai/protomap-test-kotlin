package com.acme.transport.crewing.qualification.preference

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending qualification metrics through their life cycle and keeps a journal of what it did. */
data class PendingQualificationMetricWorkflow(
    var store: PendingQualificationMetricStore,
    var validator: PendingQualificationMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingQualificationMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending qualification metric $code not found")
        validator.requireValid(item)
        item.moveTo(PendingQualificationMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending qualification metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingQualificationMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending qualification metric $code not found")
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

fun newPendingQualificationMetricWorkflow(store: PendingQualificationMetricStore, clock: Clock, flags: Registry): PendingQualificationMetricWorkflow {
    return PendingQualificationMetricWorkflow(
        store = store,
        validator = defaultPendingQualificationMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingQualificationMetricEngine(flags),
    )
}
