package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves scorecard quotas through their life cycle and keeps a journal of what it did. */
data class ScorecardQuotaService(
    var store: ScorecardQuotaStore,
    var validator: ScorecardQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScorecardQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scorecard quota $code not found")
        validator.requireValid(item)
        item.moveTo(ScorecardQuotaStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("scorecard quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScorecardQuotaStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("scorecard quota $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newScorecardQuotaService(store: ScorecardQuotaStore, clock: Clock, flags: Registry): ScorecardQuotaService {
    return ScorecardQuotaService(
        store = store,
        validator = defaultScorecardQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScorecardQuotaEngine(flags),
    )
}
