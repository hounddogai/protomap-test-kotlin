package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tiered duty period digests through their life cycle and keeps a journal of what it did. */
data class TieredDutyPeriodDigestService(
    var store: TieredDutyPeriodDigestStore,
    var validator: TieredDutyPeriodDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredDutyPeriodDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tiered duty period digest $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDutyPeriodDigestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered duty period digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDutyPeriodDigestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tiered duty period digest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newTieredDutyPeriodDigestService(store: TieredDutyPeriodDigestStore, clock: Clock, flags: Registry): TieredDutyPeriodDigestService {
    return TieredDutyPeriodDigestService(
        store = store,
        validator = defaultTieredDutyPeriodDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredDutyPeriodDigestEngine(flags),
    )
}
