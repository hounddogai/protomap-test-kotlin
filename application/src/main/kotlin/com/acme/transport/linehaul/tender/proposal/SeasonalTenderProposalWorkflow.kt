package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves seasonal tender proposals through their life cycle and keeps a journal of what it did. */
data class SeasonalTenderProposalDesk(
    var store: SeasonalTenderProposalStore,
    var validator: SeasonalTenderProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalTenderProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal tender proposal $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalTenderProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal tender proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalTenderProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("seasonal tender proposal $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalTenderProposalDesk(store: SeasonalTenderProposalStore, clock: Clock, flags: Registry): SeasonalTenderProposalDesk {
    return SeasonalTenderProposalDesk(
        store = store,
        validator = defaultSeasonalTenderProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalTenderProposalEngine(flags),
    )
}
