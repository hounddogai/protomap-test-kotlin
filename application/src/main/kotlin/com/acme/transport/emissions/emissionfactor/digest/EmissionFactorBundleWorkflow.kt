package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor bundles through their life cycle and keeps a journal of what it did. */
data class EmissionFactorBundleManager(
    var store: EmissionFactorBundleStore,
    var validator: EmissionFactorBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor bundle $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("emission factor bundle $code not found")
        val result = engine.price(item)
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

fun newEmissionFactorBundleManager(store: EmissionFactorBundleStore, clock: Clock, flags: Registry): EmissionFactorBundleManager {
    return EmissionFactorBundleManager(
        store = store,
        validator = defaultEmissionFactorBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorBundleEngine(flags),
    )
}
