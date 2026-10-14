package com.acme.transport.linehaul.tender.forecast

import com.acme.transport.linehaul.clock.Clock

/** Moves tender proposals through their life cycle and keeps a journal of what it did. */
data class TenderProposalService(
    var store: TenderProposalStore,
    var validator: TenderProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tender proposal $code not found")
        validator.requireValid(item)
        item.moveTo(TenderProposalStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("tender proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderProposalStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newTenderProposalService(store: TenderProposalStore, clock: Clock): TenderProposalService {
    return TenderProposalService(
        store = store,
        validator = defaultTenderProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
