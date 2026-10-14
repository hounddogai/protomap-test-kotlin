package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel baselines through their life cycle and keeps a journal of what it did. */
data class ParcelBaselineCoordinator(
    var store: ParcelBaselineStore,
    var validator: ParcelBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("parcel baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelBaselineStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelBaselineStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newParcelBaselineCoordinator(store: ParcelBaselineStore, clock: Clock): ParcelBaselineCoordinator {
    return ParcelBaselineCoordinator(
        store = store,
        validator = defaultParcelBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
