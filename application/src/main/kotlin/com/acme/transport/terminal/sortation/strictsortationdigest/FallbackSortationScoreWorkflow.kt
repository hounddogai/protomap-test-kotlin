package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves fallback sortation scores through their life cycle and keeps a journal of what it did. */
data class FallbackSortationScoreService(
    var store: FallbackSortationScoreStore,
    var validator: FallbackSortationScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackSortationScoreEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fallback sortation score $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackSortationScoreStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback sortation score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackSortationScoreStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback sortation score $code not found")
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

fun newFallbackSortationScoreService(store: FallbackSortationScoreStore, clock: Clock, flags: Registry): FallbackSortationScoreService {
    return FallbackSortationScoreService(
        store = store,
        validator = defaultFallbackSortationScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackSortationScoreEngine(flags),
    )
}
