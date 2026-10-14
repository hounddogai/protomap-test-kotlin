package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.clock.Clock

/** Moves tiered dispute targets through their life cycle and keeps a journal of what it did. */
data class TieredDisputeTargetService(
    var store: TieredDisputeTargetStore,
    var validator: TieredDisputeTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDisputeTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered dispute target $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDisputeTargetStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered dispute target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDisputeTargetStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered dispute target $code not found")
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

fun newTieredDisputeTargetService(store: TieredDisputeTargetStore, clock: Clock): TieredDisputeTargetService {
    return TieredDisputeTargetService(
        store = store,
        validator = defaultTieredDisputeTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDisputeTargetEngine(),
    )
}
