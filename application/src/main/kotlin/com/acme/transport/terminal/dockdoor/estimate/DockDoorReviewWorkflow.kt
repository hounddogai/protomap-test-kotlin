package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dock door reviews through their life cycle and keeps a journal of what it did. */
data class DockDoorReviewManager(
    var store: DockDoorReviewStore,
    var validator: DockDoorReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DockDoorReviewEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dock door review $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorReviewStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorReviewStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dock door review $code not found")
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

fun newDockDoorReviewManager(store: DockDoorReviewStore, clock: Clock, flags: Registry): DockDoorReviewManager {
    return DockDoorReviewManager(
        store = store,
        validator = defaultDockDoorReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDockDoorReviewEngine(flags),
    )
}
