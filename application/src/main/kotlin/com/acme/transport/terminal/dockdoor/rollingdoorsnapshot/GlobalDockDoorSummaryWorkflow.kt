package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves global dock door summaries through their life cycle and keeps a journal of what it did. */
data class GlobalDockDoorSummaryService(
    var store: GlobalDockDoorSummaryStore,
    var validator: GlobalDockDoorSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalDockDoorSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global dock door summary $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalDockDoorSummaryStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global dock door summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalDockDoorSummaryStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global dock door summary $code not found")
        val result = engine.evaluate(item)
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

fun newGlobalDockDoorSummaryService(store: GlobalDockDoorSummaryStore, clock: Clock, flags: Registry): GlobalDockDoorSummaryService {
    return GlobalDockDoorSummaryService(
        store = store,
        validator = defaultGlobalDockDoorSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalDockDoorSummaryEngine(flags),
    )
}
