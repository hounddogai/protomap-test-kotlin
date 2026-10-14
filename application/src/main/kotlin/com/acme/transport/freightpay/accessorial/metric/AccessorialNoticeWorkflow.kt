package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial notices through their life cycle and keeps a journal of what it did. */
data class AccessorialNoticeService(
    var store: AccessorialNoticeStore,
    var validator: AccessorialNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("accessorial notice $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialNoticeStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialNoticeStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newAccessorialNoticeService(store: AccessorialNoticeStore, clock: Clock): AccessorialNoticeService {
    return AccessorialNoticeService(
        store = store,
        validator = defaultAccessorialNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
