package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.clock.Clock

/** Moves emission factor checkpoints through their life cycle and keeps a journal of what it did. */
data class EmissionFactorCheckpointDesk(
    var store: EmissionFactorCheckpointStore,
    var validator: EmissionFactorCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EmissionFactorCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("emission factor checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(EmissionFactorCheckpointStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("emission factor checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EmissionFactorCheckpointStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("emission factor checkpoint $code not found")
        val result = engine.compute(item)
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

fun newEmissionFactorCheckpointDesk(store: EmissionFactorCheckpointStore, clock: Clock): EmissionFactorCheckpointDesk {
    return EmissionFactorCheckpointDesk(
        store = store,
        validator = defaultEmissionFactorCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEmissionFactorCheckpointEngine(),
    )
}
