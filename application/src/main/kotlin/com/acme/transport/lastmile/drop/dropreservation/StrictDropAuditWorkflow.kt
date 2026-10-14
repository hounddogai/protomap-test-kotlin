package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves strict drop audits through their life cycle and keeps a journal of what it did. */
data class StrictDropAuditService(
    var store: StrictDropAuditStore,
    var validator: StrictDropAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictDropAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict drop audit $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDropAuditStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict drop audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDropAuditStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("strict drop audit $code not found")
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

fun newStrictDropAuditService(store: StrictDropAuditStore, clock: Clock, flags: Registry): StrictDropAuditService {
    return StrictDropAuditService(
        store = store,
        validator = defaultStrictDropAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictDropAuditEngine(flags),
    )
}
