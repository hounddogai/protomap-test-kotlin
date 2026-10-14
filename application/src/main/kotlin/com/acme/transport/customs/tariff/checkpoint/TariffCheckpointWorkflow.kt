package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.clock.Clock

/** Moves tariff checkpoints through their life cycle and keeps a journal of what it did. */
data class TariffCheckpointService(
    var store: TariffCheckpointStore,
    var validator: TariffCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TariffCheckpointEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tariff checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(TariffCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tariff checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TariffCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tariff checkpoint $code not found")
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

fun newTariffCheckpointService(store: TariffCheckpointStore, clock: Clock): TariffCheckpointService {
    return TariffCheckpointService(
        store = store,
        validator = defaultTariffCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTariffCheckpointEngine(),
    )
}
