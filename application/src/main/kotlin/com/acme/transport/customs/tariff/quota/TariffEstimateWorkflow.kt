package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.clock.Clock

/** Moves tariff estimates through their life cycle and keeps a journal of what it did. */
data class TariffEstimateWorkflow(
    var store: TariffEstimateStore,
    var validator: TariffEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tariff estimate $code not found")
        validator.requireValid(item)
        item.moveTo(TariffEstimateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffEstimateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTariffEstimateWorkflow(store: TariffEstimateStore, clock: Clock): TariffEstimateWorkflow {
    return TariffEstimateWorkflow(
        store = store,
        validator = defaultTariffEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
