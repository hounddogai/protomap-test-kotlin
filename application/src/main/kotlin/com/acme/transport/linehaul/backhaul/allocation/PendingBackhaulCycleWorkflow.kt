package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves pending backhaul cycles through their life cycle and keeps a journal of what it did. */
data class PendingBackhaulCycleDesk(
    var store: PendingBackhaulCycleStore,
    var validator: PendingBackhaulCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingBackhaulCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending backhaul cycle $code not found")
        validator.requireValid(item)
        item.moveTo(PendingBackhaulCycleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending backhaul cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingBackhaulCycleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending backhaul cycle $code not found")
        val result = engine.price(item)
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

fun newPendingBackhaulCycleDesk(store: PendingBackhaulCycleStore, clock: Clock, flags: Registry): PendingBackhaulCycleDesk {
    return PendingBackhaulCycleDesk(
        store = store,
        validator = defaultPendingBackhaulCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingBackhaulCycleEngine(flags),
    )
}
