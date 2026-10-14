package com.acme.transport.customs.tariff.notice

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy tariff quotas through their life cycle and keeps a journal of what it did. */
data class LegacyTariffQuotaService(
    var store: LegacyTariffQuotaStore,
    var validator: LegacyTariffQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyTariffQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy tariff quota $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyTariffQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy tariff quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyTariffQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy tariff quota $code not found")
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

fun newLegacyTariffQuotaService(store: LegacyTariffQuotaStore, clock: Clock, flags: Registry): LegacyTariffQuotaService {
    return LegacyTariffQuotaService(
        store = store,
        validator = defaultLegacyTariffQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyTariffQuotaEngine(flags),
    )
}
