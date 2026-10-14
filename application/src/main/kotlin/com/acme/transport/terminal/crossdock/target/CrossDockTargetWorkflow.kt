package com.acme.transport.terminal.crossdock.target

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock targets through their life cycle and keeps a journal of what it did. */
data class CrossDockTargetService(
    var store: CrossDockTargetStore,
    var validator: CrossDockTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock target $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("cross dock target $code not found")
        val result = engine.run(item)
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

fun newCrossDockTargetService(store: CrossDockTargetStore, clock: Clock): CrossDockTargetService {
    return CrossDockTargetService(
        store = store,
        validator = defaultCrossDockTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockTargetEngine(),
    )
}
