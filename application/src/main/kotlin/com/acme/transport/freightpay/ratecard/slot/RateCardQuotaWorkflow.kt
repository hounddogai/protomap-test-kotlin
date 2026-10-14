package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rate card quotas through their life cycle and keeps a journal of what it did. */
data class RateCardQuotaCoordinator(
    var store: RateCardQuotaStore,
    var validator: RateCardQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RateCardQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rate card quota $code not found")
        validator.requireValid(item)
        item.moveTo(RateCardQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rate card quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RateCardQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rate card quota $code not found")
        val result = engine.run(item)
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

fun newRateCardQuotaCoordinator(store: RateCardQuotaStore, clock: Clock, flags: Registry): RateCardQuotaCoordinator {
    return RateCardQuotaCoordinator(
        store = store,
        validator = defaultRateCardQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRateCardQuotaEngine(flags),
    )
}
