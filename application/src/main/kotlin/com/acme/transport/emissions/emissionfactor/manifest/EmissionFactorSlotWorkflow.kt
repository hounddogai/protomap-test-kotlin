package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor slots through their life cycle and keeps a journal of what it did. */
data class EmissionFactorSlotCoordinator(
    var store: EmissionFactorSlotStore,
    var validator: EmissionFactorSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("emission factor slot $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorSlotStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorSlotStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("emission factor slot $code not found")
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

fun newEmissionFactorSlotCoordinator(store: EmissionFactorSlotStore, clock: Clock): EmissionFactorSlotCoordinator {
    return EmissionFactorSlotCoordinator(
        store = store,
        validator = defaultEmissionFactorSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorSlotEngine(),
    )
}
