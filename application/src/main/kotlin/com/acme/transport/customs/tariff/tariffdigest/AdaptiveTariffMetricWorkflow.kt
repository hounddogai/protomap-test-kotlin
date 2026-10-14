package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.customs.clock.Clock

/** Moves adaptive tariff metrics through their life cycle and keeps a journal of what it did. */
data class AdaptiveTariffMetricManager(
    var store: AdaptiveTariffMetricStore,
    var validator: AdaptiveTariffMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveTariffMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive tariff metric $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTariffMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive tariff metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTariffMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive tariff metric $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newAdaptiveTariffMetricManager(store: AdaptiveTariffMetricStore, clock: Clock): AdaptiveTariffMetricManager {
    return AdaptiveTariffMetricManager(
        store = store,
        validator = defaultAdaptiveTariffMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveTariffMetricEngine(),
    )
}
