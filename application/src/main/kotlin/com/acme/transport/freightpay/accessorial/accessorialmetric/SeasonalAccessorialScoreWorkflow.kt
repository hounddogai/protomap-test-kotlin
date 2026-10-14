package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal accessorial scores through their life cycle and keeps a journal of what it did. */
data class SeasonalAccessorialScoreService(
    var store: SeasonalAccessorialScoreStore,
    var validator: SeasonalAccessorialScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalAccessorialScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal accessorial score $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalAccessorialScoreStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal accessorial score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalAccessorialScoreStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal accessorial score $code not found")
        val result = engine.compute(item)
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

fun newSeasonalAccessorialScoreService(store: SeasonalAccessorialScoreStore, clock: Clock, flags: Registry): SeasonalAccessorialScoreService {
    return SeasonalAccessorialScoreService(
        store = store,
        validator = defaultSeasonalAccessorialScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalAccessorialScoreEngine(flags),
    )
}
