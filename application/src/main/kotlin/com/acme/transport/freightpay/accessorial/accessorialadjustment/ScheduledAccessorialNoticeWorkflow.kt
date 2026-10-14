package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.clock.Clock

/** Moves scheduled accessorial notices through their life cycle and keeps a journal of what it did. */
data class ScheduledAccessorialNoticeService(
    var store: ScheduledAccessorialNoticeStore,
    var validator: ScheduledAccessorialNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("scheduled accessorial notice $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledAccessorialNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled accessorial notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledAccessorialNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newScheduledAccessorialNoticeService(store: ScheduledAccessorialNoticeStore, clock: Clock): ScheduledAccessorialNoticeService {
    return ScheduledAccessorialNoticeService(
        store = store,
        validator = defaultScheduledAccessorialNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
