package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge milestones through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeMilestoneDesk(
    var store: FuelSurchargeMilestoneStore,
    var validator: FuelSurchargeMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel surcharge milestone $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeMilestoneStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeMilestoneStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newFuelSurchargeMilestoneDesk(store: FuelSurchargeMilestoneStore, clock: Clock): FuelSurchargeMilestoneDesk {
    return FuelSurchargeMilestoneDesk(
        store = store,
        validator = defaultFuelSurchargeMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
