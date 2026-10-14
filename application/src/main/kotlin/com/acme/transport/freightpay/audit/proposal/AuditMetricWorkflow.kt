package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves audit metrics through their life cycle and keeps a journal of what it did. */
data class AuditMetricDesk(
    var store: AuditMetricStore,
    var validator: AuditMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AuditMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("audit metric $code not found")
        validator.requireValid(item)
        item.moveTo(AuditMetricStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("audit metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AuditMetricStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("audit metric $code not found")
        val result = engine.compute(item)
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

fun newAuditMetricDesk(store: AuditMetricStore, clock: Clock, flags: Registry): AuditMetricDesk {
    return AuditMetricDesk(
        store = store,
        validator = defaultAuditMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAuditMetricEngine(flags),
    )
}
