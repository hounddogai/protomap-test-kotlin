package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal accessorial proposals through their life cycle and keeps a journal of what it did.
 */
data class SeasonalAccessorialProposalWorkflow(
    var store: SeasonalAccessorialProposalStore,
    var validator: SeasonalAccessorialProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalAccessorialProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal accessorial proposal $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAccessorialProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal accessorial proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAccessorialProposalStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("seasonal accessorial proposal $code not found")
        val result = engine.price(item)
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

fun newSeasonalAccessorialProposalWorkflow(store: SeasonalAccessorialProposalStore, clock: Clock, flags: Registry): SeasonalAccessorialProposalWorkflow {
    return SeasonalAccessorialProposalWorkflow(
        store = store,
        validator = defaultSeasonalAccessorialProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalAccessorialProposalEngine(flags),
    )
}
