package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.clock.Clock

/** Moves pending roster notices through their life cycle and keeps a journal of what it did. */
data class PendingRosterNoticeService(
    var store: PendingRosterNoticeStore,
    var validator: PendingRosterNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending roster notice $code not found")
        validator.requireValid(item)
        item.moveTo(PendingRosterNoticeStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending roster notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingRosterNoticeStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingRosterNoticeService(store: PendingRosterNoticeStore, clock: Clock): PendingRosterNoticeService {
    return PendingRosterNoticeService(
        store = store,
        validator = defaultPendingRosterNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
