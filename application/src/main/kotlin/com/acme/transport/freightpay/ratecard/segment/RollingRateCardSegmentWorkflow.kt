package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling rate card segments through their life cycle and keeps a journal of what it did. */
data class RollingRateCardSegmentManager(
    var store: RollingRateCardSegmentStore,
    var validator: RollingRateCardSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingRateCardSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling rate card segment $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRateCardSegmentStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling rate card segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRateCardSegmentStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling rate card segment $code not found")
        val result = engine.compute(item)
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

fun newRollingRateCardSegmentManager(store: RollingRateCardSegmentStore, clock: Clock, flags: Registry): RollingRateCardSegmentManager {
    return RollingRateCardSegmentManager(
        store = store,
        validator = defaultRollingRateCardSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingRateCardSegmentEngine(flags),
    )
}
