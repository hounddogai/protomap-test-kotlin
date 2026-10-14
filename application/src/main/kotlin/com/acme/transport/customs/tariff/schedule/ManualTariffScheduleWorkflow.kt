package com.acme.transport.customs.tariff.schedule

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual tariff schedules through their life cycle and keeps a journal of what it did. */
data class ManualTariffScheduleManager(
    var store: ManualTariffScheduleStore,
    var validator: ManualTariffScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTariffScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual tariff schedule $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTariffScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual tariff schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTariffScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual tariff schedule $code not found")
        val result = engine.compute(item)
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

fun newManualTariffScheduleManager(store: ManualTariffScheduleStore, clock: Clock, flags: Registry): ManualTariffScheduleManager {
    return ManualTariffScheduleManager(
        store = store,
        validator = defaultManualTariffScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTariffScheduleEngine(flags),
    )
}
