package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.clock.Clock

/** Moves default tariff agreements through their life cycle and keeps a journal of what it did. */
data class DefaultTariffAgreementWorkflow(
    var store: DefaultTariffAgreementStore,
    var validator: DefaultTariffAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTariffAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default tariff agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTariffAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default tariff agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTariffAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default tariff agreement $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newDefaultTariffAgreementWorkflow(store: DefaultTariffAgreementStore, clock: Clock): DefaultTariffAgreementWorkflow {
    return DefaultTariffAgreementWorkflow(
        store = store,
        validator = defaultDefaultTariffAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTariffAgreementEngine(),
    )
}
