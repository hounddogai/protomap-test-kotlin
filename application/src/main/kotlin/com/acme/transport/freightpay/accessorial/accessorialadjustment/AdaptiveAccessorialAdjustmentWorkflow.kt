package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves adaptive accessorial adjustments through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveAccessorialAdjustmentCoordinator(
    var store: AdaptiveAccessorialAdjustmentStore,
    var validator: AdaptiveAccessorialAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveAccessorialAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive accessorial adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAccessorialAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive accessorial adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAccessorialAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("adaptive accessorial adjustment $code not found")
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

fun newAdaptiveAccessorialAdjustmentCoordinator(store: AdaptiveAccessorialAdjustmentStore, clock: Clock): AdaptiveAccessorialAdjustmentCoordinator {
    return AdaptiveAccessorialAdjustmentCoordinator(
        store = store,
        validator = defaultAdaptiveAccessorialAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveAccessorialAdjustmentEngine(),
    )
}
