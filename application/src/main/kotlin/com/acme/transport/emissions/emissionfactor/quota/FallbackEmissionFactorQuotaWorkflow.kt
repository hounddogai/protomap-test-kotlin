package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves fallback emission factor quotas through their life cycle and keeps a journal of what it did.
 */
data class FallbackEmissionFactorQuotaManager(
    var store: FallbackEmissionFactorQuotaStore,
    var validator: FallbackEmissionFactorQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackEmissionFactorQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback emission factor quota $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackEmissionFactorQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback emission factor quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackEmissionFactorQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback emission factor quota $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newFallbackEmissionFactorQuotaManager(store: FallbackEmissionFactorQuotaStore, clock: Clock, flags: Registry): FallbackEmissionFactorQuotaManager {
    return FallbackEmissionFactorQuotaManager(
        store = store,
        validator = defaultFallbackEmissionFactorQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackEmissionFactorQuotaEngine(flags),
    )
}
