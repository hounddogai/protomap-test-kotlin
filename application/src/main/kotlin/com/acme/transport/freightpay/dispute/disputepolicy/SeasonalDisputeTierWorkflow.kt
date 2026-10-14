package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.freightpay.clock.Clock

/** Moves seasonal dispute tiers through their life cycle and keeps a journal of what it did. */
data class SeasonalDisputeTierCoordinator(
    var store: SeasonalDisputeTierStore,
    var validator: SeasonalDisputeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalDisputeTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal dispute tier $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDisputeTierStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal dispute tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDisputeTierStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal dispute tier $code not found")
        val result = engine.price(item)
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

fun newSeasonalDisputeTierCoordinator(store: SeasonalDisputeTierStore, clock: Clock): SeasonalDisputeTierCoordinator {
    return SeasonalDisputeTierCoordinator(
        store = store,
        validator = defaultSeasonalDisputeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalDisputeTierEngine(),
    )
}
