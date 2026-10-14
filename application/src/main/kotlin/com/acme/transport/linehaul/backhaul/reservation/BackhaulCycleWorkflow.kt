package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul cycles through their life cycle and keeps a journal of what it did. */
data class BackhaulCycleManager(
    var store: BackhaulCycleStore,
    var validator: BackhaulCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("backhaul cycle $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulCycleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulCycleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("backhaul cycle $code not found")
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

fun newBackhaulCycleManager(store: BackhaulCycleStore, clock: Clock, flags: Registry): BackhaulCycleManager {
    return BackhaulCycleManager(
        store = store,
        validator = defaultBackhaulCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulCycleEngine(flags),
    )
}
