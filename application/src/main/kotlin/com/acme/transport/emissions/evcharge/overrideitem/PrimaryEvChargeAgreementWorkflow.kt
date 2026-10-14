package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.clock.Clock

/** Moves primary ev charge agreements through their life cycle and keeps a journal of what it did. */
data class PrimaryEvChargeAgreementService(
    var store: PrimaryEvChargeAgreementStore,
    var validator: PrimaryEvChargeAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryEvChargeAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary ev charge agreement $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryEvChargeAgreementStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary ev charge agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryEvChargeAgreementStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary ev charge agreement $code not found")
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

fun newPrimaryEvChargeAgreementService(store: PrimaryEvChargeAgreementStore, clock: Clock): PrimaryEvChargeAgreementService {
    return PrimaryEvChargeAgreementService(
        store = store,
        validator = defaultPrimaryEvChargeAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryEvChargeAgreementEngine(),
    )
}
