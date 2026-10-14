package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor quotas through their life cycle and keeps a journal of what it did. */
data class EmissionFactorQuotaService(
    var store: EmissionFactorQuotaStore,
    var validator: EmissionFactorQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("emission factor quota $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("emission factor quota $code not found")
        val result = engine.compute(item)
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

fun newEmissionFactorQuotaService(store: EmissionFactorQuotaStore, clock: Clock, flags: Registry): EmissionFactorQuotaService {
    return EmissionFactorQuotaService(
        store = store,
        validator = defaultEmissionFactorQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorQuotaEngine(flags),
    )
}
