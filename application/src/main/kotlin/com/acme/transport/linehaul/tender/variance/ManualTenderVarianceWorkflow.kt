package com.acme.transport.linehaul.tender.variance

import com.acme.transport.linehaul.clock.Clock

/** Moves manual tender variances through their life cycle and keeps a journal of what it did. */
data class ManualTenderVarianceManager(
    var store: ManualTenderVarianceStore,
    var validator: ManualTenderVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTenderVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual tender variance $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTenderVarianceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual tender variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTenderVarianceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual tender variance $code not found")
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

fun newManualTenderVarianceManager(store: ManualTenderVarianceStore, clock: Clock): ManualTenderVarianceManager {
    return ManualTenderVarianceManager(
        store = store,
        validator = defaultManualTenderVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTenderVarianceEngine(),
    )
}
