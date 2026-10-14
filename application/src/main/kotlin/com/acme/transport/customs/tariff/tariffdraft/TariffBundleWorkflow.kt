package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.customs.clock.Clock

/** Moves tariff bundles through their life cycle and keeps a journal of what it did. */
data class TariffBundleService(
    var store: TariffBundleStore,
    var validator: TariffBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tariff bundle $code not found")
        validator.requireValid(item)
        item.moveTo(TariffBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newTariffBundleService(store: TariffBundleStore, clock: Clock): TariffBundleService {
    return TariffBundleService(
        store = store,
        validator = defaultTariffBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
