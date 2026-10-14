package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.clock.Clock

/** Moves fallback tariff estimates through their life cycle and keeps a journal of what it did. */
data class FallbackTariffEstimateDesk(
    var store: FallbackTariffEstimateStore,
    var validator: FallbackTariffEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackTariffEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback tariff estimate $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackTariffEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback tariff estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackTariffEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("fallback tariff estimate $code not found")
        val result = engine.run(item)
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

fun newFallbackTariffEstimateDesk(store: FallbackTariffEstimateStore, clock: Clock): FallbackTariffEstimateDesk {
    return FallbackTariffEstimateDesk(
        store = store,
        validator = defaultFallbackTariffEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackTariffEstimateEngine(),
    )
}
