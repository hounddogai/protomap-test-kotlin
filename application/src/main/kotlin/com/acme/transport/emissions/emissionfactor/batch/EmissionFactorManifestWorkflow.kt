package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor manifests through their life cycle and keeps a journal of what it did. */
data class EmissionFactorManifestDesk(
    var store: EmissionFactorManifestStore,
    var validator: EmissionFactorManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor manifest $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("emission factor manifest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newEmissionFactorManifestDesk(store: EmissionFactorManifestStore, clock: Clock): EmissionFactorManifestDesk {
    return EmissionFactorManifestDesk(
        store = store,
        validator = defaultEmissionFactorManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorManifestEngine(),
    )
}
