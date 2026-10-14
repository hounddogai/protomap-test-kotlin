package com.acme.transport.safety.violation.metric

import com.acme.transport.safety.clock.Clock

/** Moves manual violation metrics through their life cycle and keeps a journal of what it did. */
data class ManualViolationMetricCoordinator(
    var store: ManualViolationMetricStore,
    var validator: ManualViolationMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualViolationMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual violation metric $code not found")
        validator.requireValid(item)
        item.moveTo(ManualViolationMetricStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual violation metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualViolationMetricStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual violation metric $code not found")
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

fun newManualViolationMetricCoordinator(store: ManualViolationMetricStore, clock: Clock): ManualViolationMetricCoordinator {
    return ManualViolationMetricCoordinator(
        store = store,
        validator = defaultManualViolationMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualViolationMetricEngine(),
    )
}
