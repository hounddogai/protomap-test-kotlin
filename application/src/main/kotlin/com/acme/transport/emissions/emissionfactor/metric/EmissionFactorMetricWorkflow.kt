package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor metrics through their life cycle and keeps a journal of what it did. */
data class EmissionFactorMetricService(
    var store: EmissionFactorMetricStore,
    var validator: EmissionFactorMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("emission factor metric $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorMetricStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorMetricStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("emission factor metric $code not found")
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

fun newEmissionFactorMetricService(store: EmissionFactorMetricStore, clock: Clock): EmissionFactorMetricService {
    return EmissionFactorMetricService(
        store = store,
        validator = defaultEmissionFactorMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorMetricEngine(),
    )
}
