package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.clock.Clock

/** Moves scheduled rate card rollups through their life cycle and keeps a journal of what it did. */
data class ScheduledRateCardRollupService(
    var store: ScheduledRateCardRollupStore,
    var validator: ScheduledRateCardRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRateCardRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("scheduled rate card rollup $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRateCardRollupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled rate card rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRateCardRollupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled rate card rollup $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newScheduledRateCardRollupService(store: ScheduledRateCardRollupStore, clock: Clock): ScheduledRateCardRollupService {
    return ScheduledRateCardRollupService(
        store = store,
        validator = defaultScheduledRateCardRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRateCardRollupEngine(),
    )
}
