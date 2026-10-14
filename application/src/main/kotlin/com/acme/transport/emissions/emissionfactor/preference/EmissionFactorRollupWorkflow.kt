package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor rollups through their life cycle and keeps a journal of what it did. */
data class EmissionFactorRollupWorkflow(
    var store: EmissionFactorRollupStore,
    var validator: EmissionFactorRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("emission factor rollup $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorRollupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorRollupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("emission factor rollup $code not found")
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

fun newEmissionFactorRollupWorkflow(store: EmissionFactorRollupStore, clock: Clock): EmissionFactorRollupWorkflow {
    return EmissionFactorRollupWorkflow(
        store = store,
        validator = defaultEmissionFactorRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorRollupEngine(),
    )
}
