package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves adaptive dwell metrics through their life cycle and keeps a journal of what it did. */
data class AdaptiveDwellMetricWorkflow(
    var store: AdaptiveDwellMetricStore,
    var validator: AdaptiveDwellMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveDwellMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive dwell metric $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveDwellMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive dwell metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveDwellMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive dwell metric $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newAdaptiveDwellMetricWorkflow(store: AdaptiveDwellMetricStore, clock: Clock, flags: Registry): AdaptiveDwellMetricWorkflow {
    return AdaptiveDwellMetricWorkflow(
        store = store,
        validator = defaultAdaptiveDwellMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveDwellMetricEngine(flags),
    )
}
