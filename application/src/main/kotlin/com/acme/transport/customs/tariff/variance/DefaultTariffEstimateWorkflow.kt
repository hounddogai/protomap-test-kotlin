package com.acme.transport.customs.tariff.variance

import com.acme.transport.customs.clock.Clock

/** Moves default tariff estimates through their life cycle and keeps a journal of what it did. */
data class DefaultTariffEstimateService(
    var store: DefaultTariffEstimateStore,
    var validator: DefaultTariffEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTariffEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default tariff estimate $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTariffEstimateStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default tariff estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTariffEstimateStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default tariff estimate $code not found")
        val result = engine.evaluate(item)
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

fun newDefaultTariffEstimateService(store: DefaultTariffEstimateStore, clock: Clock): DefaultTariffEstimateService {
    return DefaultTariffEstimateService(
        store = store,
        validator = defaultDefaultTariffEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTariffEstimateEngine(),
    )
}
