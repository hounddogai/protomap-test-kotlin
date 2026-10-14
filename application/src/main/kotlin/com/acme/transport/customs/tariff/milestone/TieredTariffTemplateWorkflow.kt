package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.clock.Clock

/** Moves tiered tariff templates through their life cycle and keeps a journal of what it did. */
data class TieredTariffTemplateDesk(
    var store: TieredTariffTemplateStore,
    var validator: TieredTariffTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredTariffTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered tariff template $code not found")
        validator.requireValid(item)
        item.moveTo(TieredTariffTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered tariff template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredTariffTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tiered tariff template $code not found")
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

fun newTieredTariffTemplateDesk(store: TieredTariffTemplateStore, clock: Clock): TieredTariffTemplateDesk {
    return TieredTariffTemplateDesk(
        store = store,
        validator = defaultTieredTariffTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredTariffTemplateEngine(),
    )
}
