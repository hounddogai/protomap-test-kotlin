package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves primary carbon report policies through their life cycle and keeps a journal of what it did.
 */
data class PrimaryCarbonReportPolicyManager(
    var store: PrimaryCarbonReportPolicyStore,
    var validator: PrimaryCarbonReportPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryCarbonReportPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary carbon report policy $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCarbonReportPolicyStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary carbon report policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCarbonReportPolicyStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary carbon report policy $code not found")
        val result = engine.price(item)
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

fun newPrimaryCarbonReportPolicyManager(store: PrimaryCarbonReportPolicyStore, clock: Clock, flags: Registry): PrimaryCarbonReportPolicyManager {
    return PrimaryCarbonReportPolicyManager(
        store = store,
        validator = defaultPrimaryCarbonReportPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryCarbonReportPolicyEngine(flags),
    )
}
