package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tender metrics through their life cycle and keeps a journal of what it did. */
data class TenderMetricCoordinator(
    var store: TenderMetricStore,
    var validator: TenderMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tender metric $code not found")
        validator.requireValid(item)
        item.moveTo(TenderMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tender metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tender metric $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newTenderMetricCoordinator(store: TenderMetricStore, clock: Clock, flags: Registry): TenderMetricCoordinator {
    return TenderMetricCoordinator(
        store = store,
        validator = defaultTenderMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderMetricEngine(flags),
    )
}
