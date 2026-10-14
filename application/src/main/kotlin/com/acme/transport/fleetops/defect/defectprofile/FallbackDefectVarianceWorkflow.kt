package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves fallback defect variances through their life cycle and keeps a journal of what it did. */
data class FallbackDefectVarianceWorkflow(
    var store: FallbackDefectVarianceStore,
    var validator: FallbackDefectVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDefectVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback defect variance $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDefectVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback defect variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDefectVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fallback defect variance $code not found")
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

fun newFallbackDefectVarianceWorkflow(store: FallbackDefectVarianceStore, clock: Clock, flags: Registry): FallbackDefectVarianceWorkflow {
    return FallbackDefectVarianceWorkflow(
        store = store,
        validator = defaultFallbackDefectVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDefectVarianceEngine(flags),
    )
}
