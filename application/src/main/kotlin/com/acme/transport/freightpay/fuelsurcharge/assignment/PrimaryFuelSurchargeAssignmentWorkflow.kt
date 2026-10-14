package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves primary fuel surcharge assignments through their life cycle and keeps a journal of what it did.
 */
data class PrimaryFuelSurchargeAssignmentService(
    var store: PrimaryFuelSurchargeAssignmentStore,
    var validator: PrimaryFuelSurchargeAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary fuel surcharge assignment $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFuelSurchargeAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary fuel surcharge assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFuelSurchargeAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPrimaryFuelSurchargeAssignmentService(store: PrimaryFuelSurchargeAssignmentStore, clock: Clock): PrimaryFuelSurchargeAssignmentService {
    return PrimaryFuelSurchargeAssignmentService(
        store = store,
        validator = defaultPrimaryFuelSurchargeAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
