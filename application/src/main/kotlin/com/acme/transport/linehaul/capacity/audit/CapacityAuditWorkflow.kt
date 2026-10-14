package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capacity audits through their life cycle and keeps a journal of what it did. */
data class CapacityAuditService(
    var store: CapacityAuditStore,
    var validator: CapacityAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capacity audit $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityAuditStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityAuditStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capacity audit $code not found")
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

fun newCapacityAuditService(store: CapacityAuditStore, clock: Clock, flags: Registry): CapacityAuditService {
    return CapacityAuditService(
        store = store,
        validator = defaultCapacityAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityAuditEngine(flags),
    )
}
