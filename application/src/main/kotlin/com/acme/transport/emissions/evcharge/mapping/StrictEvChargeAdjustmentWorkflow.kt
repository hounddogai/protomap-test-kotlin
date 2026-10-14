package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.emissions.clock.Clock

/** Moves strict ev charge adjustments through their life cycle and keeps a journal of what it did. */
data class StrictEvChargeAdjustmentDesk(
    var store: StrictEvChargeAdjustmentStore,
    var validator: StrictEvChargeAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictEvChargeAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict ev charge adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(StrictEvChargeAdjustmentStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict ev charge adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictEvChargeAdjustmentStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict ev charge adjustment $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newStrictEvChargeAdjustmentDesk(store: StrictEvChargeAdjustmentStore, clock: Clock): StrictEvChargeAdjustmentDesk {
    return StrictEvChargeAdjustmentDesk(
        store = store,
        validator = defaultStrictEvChargeAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictEvChargeAdjustmentEngine(),
    )
}
