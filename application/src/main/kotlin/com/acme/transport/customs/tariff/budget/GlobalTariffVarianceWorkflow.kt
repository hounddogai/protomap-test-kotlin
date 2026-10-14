package com.acme.transport.customs.tariff.budget

import com.acme.transport.customs.clock.Clock

/** Moves global tariff variances through their life cycle and keeps a journal of what it did. */
data class GlobalTariffVarianceWorkflow(
    var store: GlobalTariffVarianceStore,
    var validator: GlobalTariffVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalTariffVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global tariff variance $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTariffVarianceStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global tariff variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTariffVarianceStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global tariff variance $code not found")
        val result = engine.price(item)
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

fun newGlobalTariffVarianceWorkflow(store: GlobalTariffVarianceStore, clock: Clock): GlobalTariffVarianceWorkflow {
    return GlobalTariffVarianceWorkflow(
        store = store,
        validator = defaultGlobalTariffVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalTariffVarianceEngine(),
    )
}
