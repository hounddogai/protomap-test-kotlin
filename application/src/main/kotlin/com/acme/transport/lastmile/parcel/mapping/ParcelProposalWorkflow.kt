package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel proposals through their life cycle and keeps a journal of what it did. */
data class ParcelProposalCoordinator(
    var store: ParcelProposalStore,
    var validator: ParcelProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("parcel proposal $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelProposalStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelProposalStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newParcelProposalCoordinator(store: ParcelProposalStore, clock: Clock): ParcelProposalCoordinator {
    return ParcelProposalCoordinator(
        store = store,
        validator = defaultParcelProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
