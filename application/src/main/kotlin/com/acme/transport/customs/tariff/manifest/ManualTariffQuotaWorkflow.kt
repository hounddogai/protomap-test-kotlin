package com.acme.transport.customs.tariff.manifest

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual tariff quotas through their life cycle and keeps a journal of what it did. */
data class ManualTariffQuotaManager(
    var store: ManualTariffQuotaStore,
    var validator: ManualTariffQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualTariffQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual tariff quota $code not found")
        validator.requireValid(item)
        item.moveTo(ManualTariffQuotaStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual tariff quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualTariffQuotaStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual tariff quota $code not found")
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

fun newManualTariffQuotaManager(store: ManualTariffQuotaStore, clock: Clock, flags: Registry): ManualTariffQuotaManager {
    return ManualTariffQuotaManager(
        store = store,
        validator = defaultManualTariffQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualTariffQuotaEngine(flags),
    )
}
