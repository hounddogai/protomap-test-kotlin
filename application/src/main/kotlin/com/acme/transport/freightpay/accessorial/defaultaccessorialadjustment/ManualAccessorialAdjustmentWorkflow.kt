package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves manual accessorial adjustments through their life cycle and keeps a journal of what it did.
 */
data class ManualAccessorialAdjustmentManager(
    var store: ManualAccessorialAdjustmentStore,
    var validator: ManualAccessorialAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualAccessorialAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual accessorial adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAccessorialAdjustmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual accessorial adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAccessorialAdjustmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual accessorial adjustment $code not found")
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

fun newManualAccessorialAdjustmentManager(store: ManualAccessorialAdjustmentStore, clock: Clock): ManualAccessorialAdjustmentManager {
    return ManualAccessorialAdjustmentManager(
        store = store,
        validator = defaultManualAccessorialAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualAccessorialAdjustmentEngine(),
    )
}
