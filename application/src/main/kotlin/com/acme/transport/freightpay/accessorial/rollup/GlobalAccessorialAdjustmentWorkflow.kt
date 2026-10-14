package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves global accessorial adjustments through their life cycle and keeps a journal of what it did.
 */
data class GlobalAccessorialAdjustmentService(
    var store: GlobalAccessorialAdjustmentStore,
    var validator: GlobalAccessorialAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global accessorial adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalAccessorialAdjustmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global accessorial adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalAccessorialAdjustmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newGlobalAccessorialAdjustmentService(store: GlobalAccessorialAdjustmentStore, clock: Clock): GlobalAccessorialAdjustmentService {
    return GlobalAccessorialAdjustmentService(
        store = store,
        validator = defaultGlobalAccessorialAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
