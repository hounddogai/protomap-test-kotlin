package com.acme.transport.lastmile.locker.tier

import com.acme.transport.lastmile.clock.Clock

/** Moves locker notices through their life cycle and keeps a journal of what it did. */
data class LockerNoticeWorkflow(
    var store: LockerNoticeStore,
    var validator: LockerNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("locker notice $code not found")
        validator.requireValid(item)
        item.moveTo(LockerNoticeStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("locker notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LockerNoticeStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newLockerNoticeWorkflow(store: LockerNoticeStore, clock: Clock): LockerNoticeWorkflow {
    return LockerNoticeWorkflow(
        store = store,
        validator = defaultLockerNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
