package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.clock.Clock

/** Moves tariff metrics through their life cycle and keeps a journal of what it did. */
data class TariffMetricDesk(
    var store: TariffMetricStore,
    var validator: TariffMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tariff metric $code not found")
        validator.requireValid(item)
        item.moveTo(TariffMetricStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffMetricStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tariff metric $code not found")
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

fun newTariffMetricDesk(store: TariffMetricStore, clock: Clock): TariffMetricDesk {
    return TariffMetricDesk(
        store = store,
        validator = defaultTariffMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffMetricEngine(),
    )
}
