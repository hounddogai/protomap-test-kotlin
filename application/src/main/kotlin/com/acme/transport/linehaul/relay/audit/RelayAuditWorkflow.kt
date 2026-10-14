package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay audits through their life cycle and keeps a journal of what it did. */
data class RelayAuditCoordinator(
    var store: RelayAuditStore,
    var validator: RelayAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("relay audit $code not found")
        validator.requireValid(item)
        item.moveTo(RelayAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("relay audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("relay audit $code not found")
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

fun newRelayAuditCoordinator(store: RelayAuditStore, clock: Clock, flags: Registry): RelayAuditCoordinator {
    return RelayAuditCoordinator(
        store = store,
        validator = defaultRelayAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayAuditEngine(flags),
    )
}
