package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves strict transit time audits through their life cycle and keeps a journal of what it did. */
data class StrictTransitTimeAuditWorkflow(
    var store: StrictTransitTimeAuditStore,
    var validator: StrictTransitTimeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTransitTimeAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict transit time audit $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTransitTimeAuditStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict transit time audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTransitTimeAuditStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict transit time audit $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newStrictTransitTimeAuditWorkflow(store: StrictTransitTimeAuditStore, clock: Clock, flags: Registry): StrictTransitTimeAuditWorkflow {
    return StrictTransitTimeAuditWorkflow(
        store = store,
        validator = defaultStrictTransitTimeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTransitTimeAuditEngine(flags),
    )
}
