package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves dispute claims through their life cycle and keeps a journal of what it did. */
data class DisputeClaimService(
    var store: DisputeClaimStore,
    var validator: DisputeClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dispute claim $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("dispute claim $code not found")
        val result = engine.run(item)
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

fun newDisputeClaimService(store: DisputeClaimStore, clock: Clock): DisputeClaimService {
    return DisputeClaimService(
        store = store,
        validator = defaultDisputeClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeClaimEngine(),
    )
}
