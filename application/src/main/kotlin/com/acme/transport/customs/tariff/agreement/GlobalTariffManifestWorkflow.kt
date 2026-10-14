package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global tariff manifests through their life cycle and keeps a journal of what it did. */
data class GlobalTariffManifestDesk(
    var store: GlobalTariffManifestStore,
    var validator: GlobalTariffManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalTariffManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global tariff manifest $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTariffManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global tariff manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTariffManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global tariff manifest $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newGlobalTariffManifestDesk(store: GlobalTariffManifestStore, clock: Clock, flags: Registry): GlobalTariffManifestDesk {
    return GlobalTariffManifestDesk(
        store = store,
        validator = defaultGlobalTariffManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalTariffManifestEngine(flags),
    )
}
