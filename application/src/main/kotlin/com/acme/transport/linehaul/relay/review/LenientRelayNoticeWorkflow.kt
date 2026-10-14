package com.acme.transport.linehaul.relay.review

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient relay notices through their life cycle and keeps a journal of what it did. */
data class LenientRelayNoticeDesk(
    var store: LenientRelayNoticeStore,
    var validator: LenientRelayNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient relay notice $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRelayNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient relay notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRelayNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLenientRelayNoticeDesk(store: LenientRelayNoticeStore, clock: Clock): LenientRelayNoticeDesk {
    return LenientRelayNoticeDesk(
        store = store,
        validator = defaultLenientRelayNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
