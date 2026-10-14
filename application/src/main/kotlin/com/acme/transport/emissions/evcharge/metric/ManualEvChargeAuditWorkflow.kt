package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual ev charge audits through their life cycle and keeps a journal of what it did. */
data class ManualEvChargeAuditService(
    var store: ManualEvChargeAuditStore,
    var validator: ManualEvChargeAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualEvChargeAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual ev charge audit $code not found")
        validator.requireValid(item)
        item.moveTo(ManualEvChargeAuditStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual ev charge audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualEvChargeAuditStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual ev charge audit $code not found")
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

fun newManualEvChargeAuditService(store: ManualEvChargeAuditStore, clock: Clock, flags: Registry): ManualEvChargeAuditService {
    return ManualEvChargeAuditService(
        store = store,
        validator = defaultManualEvChargeAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualEvChargeAuditEngine(flags),
    )
}
