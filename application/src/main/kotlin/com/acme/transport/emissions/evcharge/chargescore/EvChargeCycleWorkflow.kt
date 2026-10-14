package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves ev charge cycles through their life cycle and keeps a journal of what it did. */
data class EvChargeCycleManager(
    var store: EvChargeCycleStore,
    var validator: EvChargeCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("ev charge cycle $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("ev charge cycle $code not found")
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

fun newEvChargeCycleManager(store: EvChargeCycleStore, clock: Clock, flags: Registry): EvChargeCycleManager {
    return EvChargeCycleManager(
        store = store,
        validator = defaultEvChargeCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeCycleEngine(flags),
    )
}
