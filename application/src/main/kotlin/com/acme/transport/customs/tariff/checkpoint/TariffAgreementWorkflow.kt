package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.clock.Clock

/** Moves tariff agreements through their life cycle and keeps a journal of what it did. */
data class TariffAgreementDesk(
    var store: TariffAgreementStore,
    var validator: TariffAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tariff agreement $code not found")
        validator.requireValid(item)
        item.moveTo(TariffAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newTariffAgreementDesk(store: TariffAgreementStore, clock: Clock): TariffAgreementDesk {
    return TariffAgreementDesk(
        store = store,
        validator = defaultTariffAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
