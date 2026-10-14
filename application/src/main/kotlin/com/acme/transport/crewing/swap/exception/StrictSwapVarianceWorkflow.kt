package com.acme.transport.crewing.swap.exception

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves strict swap variances through their life cycle and keeps a journal of what it did. */
data class StrictSwapVarianceService(
    var store: StrictSwapVarianceStore,
    var validator: StrictSwapVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictSwapVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict swap variance $code not found")
        validator.requireValid(item)
        item.moveTo(StrictSwapVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict swap variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictSwapVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict swap variance $code not found")
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

fun newStrictSwapVarianceService(store: StrictSwapVarianceStore, clock: Clock, flags: Registry): StrictSwapVarianceService {
    return StrictSwapVarianceService(
        store = store,
        validator = defaultStrictSwapVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictSwapVarianceEngine(flags),
    )
}
