package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive backhaul proposals through their life cycle and keeps a journal of what it did. */
data class AdaptiveBackhaulProposalManager(
    var store: AdaptiveBackhaulProposalStore,
    var validator: AdaptiveBackhaulProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveBackhaulProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive backhaul proposal $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveBackhaulProposalStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive backhaul proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveBackhaulProposalStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive backhaul proposal $code not found")
        val result = engine.compute(item)
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

fun newAdaptiveBackhaulProposalManager(store: AdaptiveBackhaulProposalStore, clock: Clock): AdaptiveBackhaulProposalManager {
    return AdaptiveBackhaulProposalManager(
        store = store,
        validator = defaultAdaptiveBackhaulProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveBackhaulProposalEngine(),
    )
}
