package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.clock.Clock

/** Moves tariff digests through their life cycle and keeps a journal of what it did. */
data class TariffDigestDesk(
    var store: TariffDigestStore,
    var validator: TariffDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tariff digest $code not found")
        validator.requireValid(item)
        item.moveTo(TariffDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tariff digest $code not found")
        val result = engine.price(item)
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

fun newTariffDigestDesk(store: TariffDigestStore, clock: Clock): TariffDigestDesk {
    return TariffDigestDesk(
        store = store,
        validator = defaultTariffDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffDigestEngine(),
    )
}
