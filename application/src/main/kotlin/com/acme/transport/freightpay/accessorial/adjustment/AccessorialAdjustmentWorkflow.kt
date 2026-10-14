package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial adjustments through their life cycle and keeps a journal of what it did. */
data class AccessorialAdjustmentCoordinator(
    var store: AccessorialAdjustmentStore,
    var validator: AccessorialAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("accessorial adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("accessorial adjustment $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAccessorialAdjustmentCoordinator(store: AccessorialAdjustmentStore, clock: Clock): AccessorialAdjustmentCoordinator {
    return AccessorialAdjustmentCoordinator(
        store = store,
        validator = defaultAccessorialAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialAdjustmentEngine(),
    )
}
