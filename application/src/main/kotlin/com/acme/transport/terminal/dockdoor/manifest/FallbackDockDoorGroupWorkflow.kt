package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves fallback dock door groups through their life cycle and keeps a journal of what it did. */
data class FallbackDockDoorGroupManager(
    var store: FallbackDockDoorGroupStore,
    var validator: FallbackDockDoorGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackDockDoorGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback dock door group $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDockDoorGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback dock door group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDockDoorGroupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback dock door group $code not found")
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

fun newFallbackDockDoorGroupManager(store: FallbackDockDoorGroupStore, clock: Clock, flags: Registry): FallbackDockDoorGroupManager {
    return FallbackDockDoorGroupManager(
        store = store,
        validator = defaultFallbackDockDoorGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackDockDoorGroupEngine(flags),
    )
}
