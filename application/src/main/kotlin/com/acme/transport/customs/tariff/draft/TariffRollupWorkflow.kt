package com.acme.transport.customs.tariff.draft

import com.acme.transport.customs.clock.Clock

/** Moves tariff rollups through their life cycle and keeps a journal of what it did. */
data class TariffRollupService(
    var store: TariffRollupStore,
    var validator: TariffRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tariff rollup $code not found")
        validator.requireValid(item)
        item.moveTo(TariffRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tariff rollup $code not found")
        val result = engine.evaluate(item)
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

fun newTariffRollupService(store: TariffRollupStore, clock: Clock): TariffRollupService {
    return TariffRollupService(
        store = store,
        validator = defaultTariffRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffRollupEngine(),
    )
}
