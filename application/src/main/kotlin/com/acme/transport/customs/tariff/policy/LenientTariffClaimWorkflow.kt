package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient tariff claims through their life cycle and keeps a journal of what it did. */
data class LenientTariffClaimWorkflow(
    var store: LenientTariffClaimStore,
    var validator: LenientTariffClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientTariffClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient tariff claim $code not found")
        validator.requireValid(item)
        item.moveTo(LenientTariffClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient tariff claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientTariffClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient tariff claim $code not found")
        val result = engine.run(item)
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

fun newLenientTariffClaimWorkflow(store: LenientTariffClaimStore, clock: Clock, flags: Registry): LenientTariffClaimWorkflow {
    return LenientTariffClaimWorkflow(
        store = store,
        validator = defaultLenientTariffClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientTariffClaimEngine(flags),
    )
}
