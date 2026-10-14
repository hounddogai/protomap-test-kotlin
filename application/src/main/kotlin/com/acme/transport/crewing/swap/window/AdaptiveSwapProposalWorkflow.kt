package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.clock.Clock

/** Moves adaptive swap proposals through their life cycle and keeps a journal of what it did. */
data class AdaptiveSwapProposalWorkflow(
    var store: AdaptiveSwapProposalStore,
    var validator: AdaptiveSwapProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveSwapProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive swap proposal $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveSwapProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive swap proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveSwapProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive swap proposal $code not found")
        val result = engine.run(item)
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

fun newAdaptiveSwapProposalWorkflow(store: AdaptiveSwapProposalStore, clock: Clock): AdaptiveSwapProposalWorkflow {
    return AdaptiveSwapProposalWorkflow(
        store = store,
        validator = defaultAdaptiveSwapProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveSwapProposalEngine(),
    )
}
