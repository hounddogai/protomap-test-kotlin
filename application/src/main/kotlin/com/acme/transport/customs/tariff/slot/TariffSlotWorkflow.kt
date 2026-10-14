package com.acme.transport.customs.tariff.slot

import com.acme.transport.customs.clock.Clock

/** Moves tariff slots through their life cycle and keeps a journal of what it did. */
data class TariffSlotDesk(
    var store: TariffSlotStore,
    var validator: TariffSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tariff slot $code not found")
        validator.requireValid(item)
        item.moveTo(TariffSlotStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffSlotStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newTariffSlotDesk(store: TariffSlotStore, clock: Clock): TariffSlotDesk {
    return TariffSlotDesk(
        store = store,
        validator = defaultTariffSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
