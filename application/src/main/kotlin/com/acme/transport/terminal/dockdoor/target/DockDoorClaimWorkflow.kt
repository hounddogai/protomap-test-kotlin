package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door claims through their life cycle and keeps a journal of what it did. */
data class DockDoorClaimWorkflow(
    var store: DockDoorClaimStore,
    var validator: DockDoorClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dock door claim $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dock door claim $code not found")
        val result = engine.evaluate(item)
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

fun newDockDoorClaimWorkflow(store: DockDoorClaimStore, clock: Clock, flags: Registry): DockDoorClaimWorkflow {
    return DockDoorClaimWorkflow(
        store = store,
        validator = defaultDockDoorClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorClaimEngine(flags),
    )
}
