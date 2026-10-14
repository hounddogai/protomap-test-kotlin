package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves tiered scorecard quotas through their life cycle and keeps a journal of what it did. */
data class TieredScorecardQuotaService(
    var store: TieredScorecardQuotaStore,
    var validator: TieredScorecardQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredScorecardQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered scorecard quota $code not found")
        validator.requireValid(item)
        item.moveTo(TieredScorecardQuotaStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered scorecard quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredScorecardQuotaStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered scorecard quota $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newTieredScorecardQuotaService(store: TieredScorecardQuotaStore, clock: Clock, flags: Registry): TieredScorecardQuotaService {
    return TieredScorecardQuotaService(
        store = store,
        validator = defaultTieredScorecardQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredScorecardQuotaEngine(flags),
    )
}
