package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves tiered fuel surcharge summaries through their life cycle and keeps a journal of what it did.
 */
data class TieredFuelSurchargeSummaryManager(
    var store: TieredFuelSurchargeSummaryStore,
    var validator: TieredFuelSurchargeSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TieredFuelSurchargeSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge summary $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelSurchargeSummaryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelSurchargeSummaryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tiered fuel surcharge summary $code not found")
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

fun newTieredFuelSurchargeSummaryManager(store: TieredFuelSurchargeSummaryStore, clock: Clock, flags: Registry): TieredFuelSurchargeSummaryManager {
    return TieredFuelSurchargeSummaryManager(
        store = store,
        validator = defaultTieredFuelSurchargeSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTieredFuelSurchargeSummaryEngine(flags),
    )
}
