package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.clock.Clock

/** Moves primary fuel surcharge tiers through their life cycle and keeps a journal of what it did. */
data class PrimaryFuelSurchargeTierCoordinator(
    var store: PrimaryFuelSurchargeTierStore,
    var validator: PrimaryFuelSurchargeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary fuel surcharge tier $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFuelSurchargeTierStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary fuel surcharge tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFuelSurchargeTierStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryFuelSurchargeTierCoordinator(store: PrimaryFuelSurchargeTierStore, clock: Clock): PrimaryFuelSurchargeTierCoordinator {
    return PrimaryFuelSurchargeTierCoordinator(
        store = store,
        validator = defaultPrimaryFuelSurchargeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
