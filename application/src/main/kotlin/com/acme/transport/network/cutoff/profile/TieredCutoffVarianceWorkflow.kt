package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves tiered cutoff variances through their life cycle and keeps a journal of what it did. */
data class TieredCutoffVarianceService(
    var store: TieredCutoffVarianceStore,
    var validator: TieredCutoffVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredCutoffVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered cutoff variance $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCutoffVarianceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered cutoff variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCutoffVarianceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("tiered cutoff variance $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newTieredCutoffVarianceService(store: TieredCutoffVarianceStore, clock: Clock, flags: Registry): TieredCutoffVarianceService {
    return TieredCutoffVarianceService(
        store = store,
        validator = defaultTieredCutoffVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredCutoffVarianceEngine(flags),
    )
}
