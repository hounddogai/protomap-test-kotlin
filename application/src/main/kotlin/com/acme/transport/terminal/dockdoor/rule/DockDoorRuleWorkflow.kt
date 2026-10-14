package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door rules through their life cycle and keeps a journal of what it did. */
data class DockDoorRuleService(
    var store: DockDoorRuleStore,
    var validator: DockDoorRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dock door rule $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorRuleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorRuleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door rule $code not found")
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

fun newDockDoorRuleService(store: DockDoorRuleStore, clock: Clock, flags: Registry): DockDoorRuleService {
    return DockDoorRuleService(
        store = store,
        validator = defaultDockDoorRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorRuleEngine(flags),
    )
}
