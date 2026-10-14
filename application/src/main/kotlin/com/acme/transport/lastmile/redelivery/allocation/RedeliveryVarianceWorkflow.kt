package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery variances through their life cycle and keeps a journal of what it did. */
data class RedeliveryVarianceService(
    var store: RedeliveryVarianceStore,
    var validator: RedeliveryVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RedeliveryVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("redelivery variance $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryVarianceStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("redelivery variance $code not found")
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

fun newRedeliveryVarianceService(store: RedeliveryVarianceStore, clock: Clock): RedeliveryVarianceService {
    return RedeliveryVarianceService(
        store = store,
        validator = defaultRedeliveryVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRedeliveryVarianceEngine(),
    )
}
