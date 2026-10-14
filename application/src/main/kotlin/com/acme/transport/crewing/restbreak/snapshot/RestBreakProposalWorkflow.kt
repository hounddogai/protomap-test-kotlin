package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rest break proposals through their life cycle and keeps a journal of what it did. */
data class RestBreakProposalService(
    var store: RestBreakProposalStore,
    var validator: RestBreakProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RestBreakProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rest break proposal $code not found")
        validator.requireValid(item)
        item.moveTo(RestBreakProposalStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rest break proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RestBreakProposalStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rest break proposal $code not found")
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

fun newRestBreakProposalService(store: RestBreakProposalStore, clock: Clock, flags: Registry): RestBreakProposalService {
    return RestBreakProposalService(
        store = store,
        validator = defaultRestBreakProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRestBreakProposalEngine(flags),
    )
}
