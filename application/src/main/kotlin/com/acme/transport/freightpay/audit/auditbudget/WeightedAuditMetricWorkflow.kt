package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.clock.Clock

/** Moves weighted audit metrics through their life cycle and keeps a journal of what it did. */
data class WeightedAuditMetricService(
    var store: WeightedAuditMetricStore,
    var validator: WeightedAuditMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedAuditMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("weighted audit metric $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedAuditMetricStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted audit metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedAuditMetricStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted audit metric $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newWeightedAuditMetricService(store: WeightedAuditMetricStore, clock: Clock): WeightedAuditMetricService {
    return WeightedAuditMetricService(
        store = store,
        validator = defaultWeightedAuditMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedAuditMetricEngine(),
    )
}
