package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global emission factor slots through their life cycle and keeps a journal of what it did. */
data class GlobalEmissionFactorSlotService(
    var store: GlobalEmissionFactorSlotStore,
    var validator: GlobalEmissionFactorSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalEmissionFactorSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global emission factor slot $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalEmissionFactorSlotStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global emission factor slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalEmissionFactorSlotStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global emission factor slot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newGlobalEmissionFactorSlotService(store: GlobalEmissionFactorSlotStore, clock: Clock, flags: Registry): GlobalEmissionFactorSlotService {
    return GlobalEmissionFactorSlotService(
        store = store,
        validator = defaultGlobalEmissionFactorSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalEmissionFactorSlotEngine(flags),
    )
}
