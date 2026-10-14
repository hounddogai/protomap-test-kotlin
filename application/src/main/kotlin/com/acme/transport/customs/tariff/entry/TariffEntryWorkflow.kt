package com.acme.transport.customs.tariff.entry

import com.acme.transport.customs.clock.Clock

/** Moves tariff entries through their life cycle and keeps a journal of what it did. */
data class TariffEntryDesk(
    var store: TariffEntryStore,
    var validator: TariffEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tariff entry $code not found")
        validator.requireValid(item)
        item.moveTo(TariffEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tariff entry $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newTariffEntryDesk(store: TariffEntryStore, clock: Clock): TariffEntryDesk {
    return TariffEntryDesk(
        store = store,
        validator = defaultTariffEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffEntryEngine(),
    )
}
