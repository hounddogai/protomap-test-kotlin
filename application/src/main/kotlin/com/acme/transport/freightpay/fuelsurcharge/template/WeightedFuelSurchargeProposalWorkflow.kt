package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves weighted fuel surcharge proposals through their life cycle and keeps a journal of what it did.
 */
data class WeightedFuelSurchargeProposalCoordinator(
    var store: WeightedFuelSurchargeProposalStore,
    var validator: WeightedFuelSurchargeProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted fuel surcharge proposal $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedFuelSurchargeProposalStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted fuel surcharge proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedFuelSurchargeProposalStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newWeightedFuelSurchargeProposalCoordinator(store: WeightedFuelSurchargeProposalStore, clock: Clock): WeightedFuelSurchargeProposalCoordinator {
    return WeightedFuelSurchargeProposalCoordinator(
        store = store,
        validator = defaultWeightedFuelSurchargeProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
