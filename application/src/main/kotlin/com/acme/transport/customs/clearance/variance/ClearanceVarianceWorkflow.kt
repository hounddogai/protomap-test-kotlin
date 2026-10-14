package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance variances through their life cycle and keeps a journal of what it did. */
data class ClearanceVarianceDesk(
    var store: ClearanceVarianceStore,
    var validator: ClearanceVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("clearance variance $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("clearance variance $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newClearanceVarianceDesk(store: ClearanceVarianceStore, clock: Clock, flags: Registry): ClearanceVarianceDesk {
    return ClearanceVarianceDesk(
        store = store,
        validator = defaultClearanceVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceVarianceEngine(flags),
    )
}
