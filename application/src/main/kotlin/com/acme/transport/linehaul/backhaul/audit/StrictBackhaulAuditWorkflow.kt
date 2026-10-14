package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves strict backhaul audits through their life cycle and keeps a journal of what it did. */
data class StrictBackhaulAuditManager(
    var store: StrictBackhaulAuditStore,
    var validator: StrictBackhaulAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictBackhaulAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict backhaul audit $code not found")
        validator.requireValid(item)
        item.moveTo(StrictBackhaulAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict backhaul audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictBackhaulAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict backhaul audit $code not found")
        val result = engine.run(item)
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

fun newStrictBackhaulAuditManager(store: StrictBackhaulAuditStore, clock: Clock, flags: Registry): StrictBackhaulAuditManager {
    return StrictBackhaulAuditManager(
        store = store,
        validator = defaultStrictBackhaulAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictBackhaulAuditEngine(flags),
    )
}
