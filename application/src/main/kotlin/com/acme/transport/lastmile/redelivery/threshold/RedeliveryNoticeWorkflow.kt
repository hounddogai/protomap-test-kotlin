package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.lastmile.clock.Clock

/** Moves redelivery notices through their life cycle and keeps a journal of what it did. */
data class RedeliveryNoticeDesk(
    var store: RedeliveryNoticeStore,
    var validator: RedeliveryNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("redelivery notice $code not found")
        validator.requireValid(item)
        item.moveTo(RedeliveryNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("redelivery notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RedeliveryNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newRedeliveryNoticeDesk(store: RedeliveryNoticeStore, clock: Clock): RedeliveryNoticeDesk {
    return RedeliveryNoticeDesk(
        store = store,
        validator = defaultRedeliveryNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
