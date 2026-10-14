package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive availability audits through their life cycle and keeps a journal of what it did. */
data class AdaptiveAvailabilityAuditDesk(
    var store: AdaptiveAvailabilityAuditStore,
    var validator: AdaptiveAvailabilityAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveAvailabilityAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive availability audit $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAvailabilityAuditStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive availability audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAvailabilityAuditStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive availability audit $code not found")
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

fun newAdaptiveAvailabilityAuditDesk(store: AdaptiveAvailabilityAuditStore, clock: Clock, flags: Registry): AdaptiveAvailabilityAuditDesk {
    return AdaptiveAvailabilityAuditDesk(
        store = store,
        validator = defaultAdaptiveAvailabilityAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveAvailabilityAuditEngine(flags),
    )
}
