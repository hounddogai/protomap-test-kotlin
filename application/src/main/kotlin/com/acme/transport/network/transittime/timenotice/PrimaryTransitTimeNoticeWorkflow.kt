package com.acme.transport.network.transittime.timenotice

import com.acme.transport.network.clock.Clock

/** Moves primary transit time notices through their life cycle and keeps a journal of what it did. */
data class PrimaryTransitTimeNoticeDesk(
    var store: PrimaryTransitTimeNoticeStore,
    var validator: PrimaryTransitTimeNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary transit time notice $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryTransitTimeNoticeStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary transit time notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryTransitTimeNoticeStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newPrimaryTransitTimeNoticeDesk(store: PrimaryTransitTimeNoticeStore, clock: Clock): PrimaryTransitTimeNoticeDesk {
    return PrimaryTransitTimeNoticeDesk(
        store = store,
        validator = defaultPrimaryTransitTimeNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
