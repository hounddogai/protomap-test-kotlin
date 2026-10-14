package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dwell assignments through their life cycle and keeps a journal of what it did. */
data class DwellAssignmentManager(
    var store: DwellAssignmentStore,
    var validator: DwellAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("dwell assignment $code not found")
        validator.requireValid(item)
        item.moveTo(DwellAssignmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellAssignmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dwell assignment $code not found")
        val result = engine.compute(item)
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

fun newDwellAssignmentManager(store: DwellAssignmentStore, clock: Clock, flags: Registry): DwellAssignmentManager {
    return DwellAssignmentManager(
        store = store,
        validator = defaultDwellAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellAssignmentEngine(flags),
    )
}
