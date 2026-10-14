package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves seasonal proof of delivery plans through their life cycle and keeps a journal of what it did.
 */
data class SeasonalProofOfDeliveryPlanService(
    var store: SeasonalProofOfDeliveryPlanStore,
    var validator: SeasonalProofOfDeliveryPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalProofOfDeliveryPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("seasonal proof of delivery plan $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalProofOfDeliveryPlanStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal proof of delivery plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalProofOfDeliveryPlanStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal proof of delivery plan $code not found")
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

fun newSeasonalProofOfDeliveryPlanService(store: SeasonalProofOfDeliveryPlanStore, clock: Clock, flags: Registry): SeasonalProofOfDeliveryPlanService {
    return SeasonalProofOfDeliveryPlanService(
        store = store,
        validator = defaultSeasonalProofOfDeliveryPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalProofOfDeliveryPlanEngine(flags),
    )
}
