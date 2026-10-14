package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves rolling dwell cycles through their life cycle and keeps a journal of what it did. */
data class RollingDwellCycleCoordinator(
    var store: RollingDwellCycleStore,
    var validator: RollingDwellCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDwellCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling dwell cycle $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDwellCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling dwell cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDwellCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling dwell cycle $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRollingDwellCycleCoordinator(store: RollingDwellCycleStore, clock: Clock, flags: Registry): RollingDwellCycleCoordinator {
    return RollingDwellCycleCoordinator(
        store = store,
        validator = defaultRollingDwellCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDwellCycleEngine(flags),
    )
}
