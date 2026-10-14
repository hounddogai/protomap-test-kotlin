package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.customs.clock.Clock

/** Moves regional tariff overrides through their life cycle and keeps a journal of what it did. */
data class RegionalTariffOverrideService(
    var store: RegionalTariffOverrideStore,
    var validator: RegionalTariffOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional tariff override $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalTariffOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional tariff override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalTariffOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newRegionalTariffOverrideService(store: RegionalTariffOverrideStore, clock: Clock): RegionalTariffOverrideService {
    return RegionalTariffOverrideService(
        store = store,
        validator = defaultRegionalTariffOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
