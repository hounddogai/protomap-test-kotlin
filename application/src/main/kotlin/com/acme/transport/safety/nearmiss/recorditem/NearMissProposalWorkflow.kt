package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves near miss proposals through their life cycle and keeps a journal of what it did. */
data class NearMissProposalCoordinator(
    var store: NearMissProposalStore,
    var validator: NearMissProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss proposal $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissProposalStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissProposalStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("near miss proposal $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newNearMissProposalCoordinator(store: NearMissProposalStore, clock: Clock, flags: Registry): NearMissProposalCoordinator {
    return NearMissProposalCoordinator(
        store = store,
        validator = defaultNearMissProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissProposalEngine(flags),
    )
}
