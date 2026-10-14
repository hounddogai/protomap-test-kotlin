package com.acme.transport.customs.tariff.policy

import com.acme.transport.customs.clock.Clock

/** Moves lenient tariff policies through their life cycle and keeps a journal of what it did. */
data class LenientTariffPolicyCoordinator(
    var store: LenientTariffPolicyStore,
    var validator: LenientTariffPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient tariff policy $code not found")
        validator.requireValid(item)
        item.moveTo(LenientTariffPolicyStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient tariff policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientTariffPolicyStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newLenientTariffPolicyCoordinator(store: LenientTariffPolicyStore, clock: Clock): LenientTariffPolicyCoordinator {
    return LenientTariffPolicyCoordinator(
        store = store,
        validator = defaultLenientTariffPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
