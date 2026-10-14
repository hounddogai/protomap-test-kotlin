package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.terminal.clock.Clock

/** Moves dock door allocations through their life cycle and keeps a journal of what it did. */
data class DockDoorAllocationService(
    var store: DockDoorAllocationStore,
    var validator: DockDoorAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dock door allocation $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorAllocationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorAllocationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door allocation $code not found")
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

fun newDockDoorAllocationService(store: DockDoorAllocationStore, clock: Clock): DockDoorAllocationService {
    return DockDoorAllocationService(
        store = store,
        validator = defaultDockDoorAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorAllocationEngine(),
    )
}
