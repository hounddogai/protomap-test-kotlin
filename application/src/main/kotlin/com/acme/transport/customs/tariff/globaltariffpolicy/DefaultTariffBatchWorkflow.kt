package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves default tariff batches through their life cycle and keeps a journal of what it did. */
data class DefaultTariffBatchService(
    var store: DefaultTariffBatchStore,
    var validator: DefaultTariffBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultTariffBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default tariff batch $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultTariffBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("default tariff batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultTariffBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default tariff batch $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultTariffBatchService(store: DefaultTariffBatchStore, clock: Clock, flags: Registry): DefaultTariffBatchService {
    return DefaultTariffBatchService(
        store = store,
        validator = defaultDefaultTariffBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultTariffBatchEngine(flags),
    )
}
