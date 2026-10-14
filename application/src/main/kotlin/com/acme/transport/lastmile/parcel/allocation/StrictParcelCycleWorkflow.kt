package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves strict parcel cycles through their life cycle and keeps a journal of what it did. */
data class StrictParcelCycleService(
    var store: StrictParcelCycleStore,
    var validator: StrictParcelCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictParcelCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict parcel cycle $code not found")
        validator.requireValid(item)
        item.moveTo(StrictParcelCycleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict parcel cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictParcelCycleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("strict parcel cycle $code not found")
        val result = engine.price(item)
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

fun newStrictParcelCycleService(store: StrictParcelCycleStore, clock: Clock, flags: Registry): StrictParcelCycleService {
    return StrictParcelCycleService(
        store = store,
        validator = defaultStrictParcelCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictParcelCycleEngine(flags),
    )
}
