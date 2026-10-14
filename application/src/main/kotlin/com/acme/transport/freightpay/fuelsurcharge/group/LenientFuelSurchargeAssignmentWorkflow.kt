package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves lenient fuel surcharge assignments through their life cycle and keeps a journal of what it did.
 */
data class LenientFuelSurchargeAssignmentCoordinator(
    var store: LenientFuelSurchargeAssignmentStore,
    var validator: LenientFuelSurchargeAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient fuel surcharge assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientFuelSurchargeAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient fuel surcharge assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientFuelSurchargeAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newLenientFuelSurchargeAssignmentCoordinator(store: LenientFuelSurchargeAssignmentStore, clock: Clock): LenientFuelSurchargeAssignmentCoordinator {
    return LenientFuelSurchargeAssignmentCoordinator(
        store = store,
        validator = defaultLenientFuelSurchargeAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
