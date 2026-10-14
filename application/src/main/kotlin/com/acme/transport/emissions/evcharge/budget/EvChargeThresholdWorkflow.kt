package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge thresholds through their life cycle and keeps a journal of what it did. */
data class EvChargeThresholdDesk(
    var store: EvChargeThresholdStore,
    var validator: EvChargeThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("ev charge threshold $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeThresholdStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeThresholdStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("ev charge threshold $code not found")
        val result = engine.price(item)
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

fun newEvChargeThresholdDesk(store: EvChargeThresholdStore, clock: Clock): EvChargeThresholdDesk {
    return EvChargeThresholdDesk(
        store = store,
        validator = defaultEvChargeThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeThresholdEngine(),
    )
}
