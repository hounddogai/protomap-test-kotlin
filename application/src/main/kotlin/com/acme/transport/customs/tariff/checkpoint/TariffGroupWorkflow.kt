package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.clock.Clock

/** Moves tariff groups through their life cycle and keeps a journal of what it did. */
data class TariffGroupDesk(
    var store: TariffGroupStore,
    var validator: TariffGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tariff group $code not found")
        validator.requireValid(item)
        item.moveTo(TariffGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tariff group $code not found")
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

fun newTariffGroupDesk(store: TariffGroupStore, clock: Clock): TariffGroupDesk {
    return TariffGroupDesk(
        store = store,
        validator = defaultTariffGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffGroupEngine(),
    )
}
