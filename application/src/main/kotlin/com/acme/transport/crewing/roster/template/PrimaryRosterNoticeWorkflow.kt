package com.acme.transport.crewing.roster.template

import com.acme.transport.crewing.clock.Clock

/** Moves primary roster notices through their life cycle and keeps a journal of what it did. */
data class PrimaryRosterNoticeService(
    var store: PrimaryRosterNoticeStore,
    var validator: PrimaryRosterNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary roster notice $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryRosterNoticeStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary roster notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryRosterNoticeStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryRosterNoticeService(store: PrimaryRosterNoticeStore, clock: Clock): PrimaryRosterNoticeService {
    return PrimaryRosterNoticeService(
        store = store,
        validator = defaultPrimaryRosterNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
