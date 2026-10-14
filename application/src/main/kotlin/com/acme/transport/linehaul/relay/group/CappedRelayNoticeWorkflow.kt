package com.acme.transport.linehaul.relay.group

import com.acme.transport.linehaul.clock.Clock

/** Moves capped relay notices through their life cycle and keeps a journal of what it did. */
data class CappedRelayNoticeDesk(
    var store: CappedRelayNoticeStore,
    var validator: CappedRelayNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped relay notice $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRelayNoticeStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped relay notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRelayNoticeStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newCappedRelayNoticeDesk(store: CappedRelayNoticeStore, clock: Clock): CappedRelayNoticeDesk {
    return CappedRelayNoticeDesk(
        store = store,
        validator = defaultCappedRelayNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
