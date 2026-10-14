package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves default tender metrics through their life cycle and keeps a journal of what it did. */
data class DefaultTenderMetricService(
    var store: DefaultTenderMetricStore,
    var validator: DefaultTenderMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTenderMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default tender metric $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTenderMetricStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default tender metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTenderMetricStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default tender metric $code not found")
        val result = engine.price(item)
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

fun newDefaultTenderMetricService(store: DefaultTenderMetricStore, clock: Clock, flags: Registry): DefaultTenderMetricService {
    return DefaultTenderMetricService(
        store = store,
        validator = defaultDefaultTenderMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTenderMetricEngine(flags),
    )
}
