package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire cycles through their life cycle and keeps a journal of what it did. */
data class TireCycleDesk(
    var store: TireCycleStore,
    var validator: TireCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tire cycle $code not found")
        validator.requireValid(item)
        item.moveTo(TireCycleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tire cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireCycleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tire cycle $code not found")
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

fun newTireCycleDesk(store: TireCycleStore, clock: Clock, flags: Registry): TireCycleDesk {
    return TireCycleDesk(
        store = store,
        validator = defaultTireCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireCycleEngine(flags),
    )
}
