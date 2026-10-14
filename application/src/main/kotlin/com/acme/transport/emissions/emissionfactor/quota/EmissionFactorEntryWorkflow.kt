package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves emission factor entries through their life cycle and keeps a journal of what it did. */
data class EmissionFactorEntryCoordinator(
    var store: EmissionFactorEntryStore,
    var validator: EmissionFactorEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("emission factor entry $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("emission factor entry $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newEmissionFactorEntryCoordinator(store: EmissionFactorEntryStore, clock: Clock, flags: Registry): EmissionFactorEntryCoordinator {
    return EmissionFactorEntryCoordinator(
        store = store,
        validator = defaultEmissionFactorEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorEntryEngine(flags),
    )
}
