package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.clock.Clock

/** Moves tariff thresholds through their life cycle and keeps a journal of what it did. */
data class TariffThresholdDesk(
    var store: TariffThresholdStore,
    var validator: TariffThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tariff threshold $code not found")
        validator.requireValid(item)
        item.moveTo(TariffThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTariffThresholdDesk(store: TariffThresholdStore, clock: Clock): TariffThresholdDesk {
    return TariffThresholdDesk(
        store = store,
        validator = defaultTariffThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
