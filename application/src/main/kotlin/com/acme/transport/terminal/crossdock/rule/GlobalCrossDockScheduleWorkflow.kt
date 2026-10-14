package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves global cross dock schedules through their life cycle and keeps a journal of what it did. */
data class GlobalCrossDockScheduleDesk(
    var store: GlobalCrossDockScheduleStore,
    var validator: GlobalCrossDockScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalCrossDockScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global cross dock schedule $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalCrossDockScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global cross dock schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalCrossDockScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global cross dock schedule $code not found")
        val result = engine.compute(item)
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

fun newGlobalCrossDockScheduleDesk(store: GlobalCrossDockScheduleStore, clock: Clock, flags: Registry): GlobalCrossDockScheduleDesk {
    return GlobalCrossDockScheduleDesk(
        store = store,
        validator = defaultGlobalCrossDockScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalCrossDockScheduleEngine(flags),
    )
}
