package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.customs.clock.Clock

/** Moves default tariff targets through their life cycle and keeps a journal of what it did. */
data class DefaultTariffTargetManager(
    var store: DefaultTariffTargetStore,
    var validator: DefaultTariffTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default tariff target $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTariffTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default tariff target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTariffTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newDefaultTariffTargetManager(store: DefaultTariffTargetStore, clock: Clock): DefaultTariffTargetManager {
    return DefaultTariffTargetManager(
        store = store,
        validator = defaultDefaultTariffTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
