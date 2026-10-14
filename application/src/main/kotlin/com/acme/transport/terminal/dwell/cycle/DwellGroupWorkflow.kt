package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves dwell groups through their life cycle and keeps a journal of what it did. */
data class DwellGroupWorkflow(
    var store: DwellGroupStore,
    var validator: DwellGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell group $code not found")
        validator.requireValid(item)
        item.moveTo(DwellGroupStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellGroupStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dwell group $code not found")
        val result = engine.run(item)
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

fun newDwellGroupWorkflow(store: DwellGroupStore, clock: Clock, flags: Registry): DwellGroupWorkflow {
    return DwellGroupWorkflow(
        store = store,
        validator = defaultDwellGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellGroupEngine(flags),
    )
}
