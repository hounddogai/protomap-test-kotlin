package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending tariff agreements through their life cycle and keeps a journal of what it did. */
data class PendingTariffAgreementManager(
    var store: PendingTariffAgreementStore,
    var validator: PendingTariffAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTariffAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending tariff agreement $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTariffAgreementStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tariff agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTariffAgreementStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending tariff agreement $code not found")
        val result = engine.run(item)
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

fun newPendingTariffAgreementManager(store: PendingTariffAgreementStore, clock: Clock, flags: Registry): PendingTariffAgreementManager {
    return PendingTariffAgreementManager(
        store = store,
        validator = defaultPendingTariffAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTariffAgreementEngine(flags),
    )
}
