package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves global trailer proposals through their life cycle and keeps a journal of what it did. */
data class GlobalTrailerProposalDesk(
    var store: GlobalTrailerProposalStore,
    var validator: GlobalTrailerProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalTrailerProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global trailer proposal $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTrailerProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global trailer proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTrailerProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global trailer proposal $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newGlobalTrailerProposalDesk(store: GlobalTrailerProposalStore, clock: Clock, flags: Registry): GlobalTrailerProposalDesk {
    return GlobalTrailerProposalDesk(
        store = store,
        validator = defaultGlobalTrailerProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalTrailerProposalEngine(flags),
    )
}
