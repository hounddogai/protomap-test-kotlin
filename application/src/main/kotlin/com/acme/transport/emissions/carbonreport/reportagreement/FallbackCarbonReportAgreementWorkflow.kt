package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback carbon report agreements through their life cycle and keeps a journal of what it did.
 */
data class FallbackCarbonReportAgreementWorkflow(
    var store: FallbackCarbonReportAgreementStore,
    var validator: FallbackCarbonReportAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCarbonReportAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback carbon report agreement $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCarbonReportAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback carbon report agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCarbonReportAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback carbon report agreement $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newFallbackCarbonReportAgreementWorkflow(store: FallbackCarbonReportAgreementStore, clock: Clock, flags: Registry): FallbackCarbonReportAgreementWorkflow {
    return FallbackCarbonReportAgreementWorkflow(
        store = store,
        validator = defaultFallbackCarbonReportAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCarbonReportAgreementEngine(flags),
    )
}
