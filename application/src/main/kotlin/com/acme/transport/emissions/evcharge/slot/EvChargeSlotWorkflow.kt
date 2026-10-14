package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge slots through their life cycle and keeps a journal of what it did. */
data class EvChargeSlotService(
    var store: EvChargeSlotStore,
    var validator: EvChargeSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("ev charge slot $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeSlotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeSlotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("ev charge slot $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newEvChargeSlotService(store: EvChargeSlotStore, clock: Clock, flags: Registry): EvChargeSlotService {
    return EvChargeSlotService(
        store = store,
        validator = defaultEvChargeSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeSlotEngine(flags),
    )
}
