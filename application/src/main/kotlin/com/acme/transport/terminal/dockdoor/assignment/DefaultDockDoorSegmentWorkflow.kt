package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.terminal.clock.Clock

/** Moves default dock door segments through their life cycle and keeps a journal of what it did. */
data class DefaultDockDoorSegmentService(
    var store: DefaultDockDoorSegmentStore,
    var validator: DefaultDockDoorSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultDockDoorSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default dock door segment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultDockDoorSegmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default dock door segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultDockDoorSegmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("default dock door segment $code not found")
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

fun newDefaultDockDoorSegmentService(store: DefaultDockDoorSegmentStore, clock: Clock): DefaultDockDoorSegmentService {
    return DefaultDockDoorSegmentService(
        store = store,
        validator = defaultDefaultDockDoorSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultDockDoorSegmentEngine(),
    )
}
