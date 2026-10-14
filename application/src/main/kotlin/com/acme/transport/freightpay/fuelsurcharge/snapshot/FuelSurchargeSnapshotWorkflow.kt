package com.acme.transport.freightpay.fuelsurcharge.snapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge snapshots through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeSnapshotService(
    var store: FuelSurchargeSnapshotStore,
    var validator: FuelSurchargeSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fuel surcharge snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeSnapshotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeSnapshotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newFuelSurchargeSnapshotService(store: FuelSurchargeSnapshotStore, clock: Clock): FuelSurchargeSnapshotService {
    return FuelSurchargeSnapshotService(
        store = store,
        validator = defaultFuelSurchargeSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
