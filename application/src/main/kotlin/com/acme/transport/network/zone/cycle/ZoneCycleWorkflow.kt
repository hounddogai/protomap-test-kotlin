package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves zone cycles through their life cycle and keeps a journal of what it did. */
data class ZoneCycleCoordinator(
    var store: ZoneCycleStore,
    var validator: ZoneCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("zone cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneCycleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("zone cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneCycleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("zone cycle $code not found")
        val result = engine.compute(item)
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

fun newZoneCycleCoordinator(store: ZoneCycleStore, clock: Clock, flags: Registry): ZoneCycleCoordinator {
    return ZoneCycleCoordinator(
        store = store,
        validator = defaultZoneCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneCycleEngine(flags),
    )
}
