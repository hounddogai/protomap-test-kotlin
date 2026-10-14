package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves clearance groups through their life cycle and keeps a journal of what it did. */
data class ClearanceGroupDesk(
    var store: ClearanceGroupStore,
    var validator: ClearanceGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("clearance group $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("clearance group $code not found")
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

fun newClearanceGroupDesk(store: ClearanceGroupStore, clock: Clock, flags: Registry): ClearanceGroupDesk {
    return ClearanceGroupDesk(
        store = store,
        validator = defaultClearanceGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceGroupEngine(flags),
    )
}
