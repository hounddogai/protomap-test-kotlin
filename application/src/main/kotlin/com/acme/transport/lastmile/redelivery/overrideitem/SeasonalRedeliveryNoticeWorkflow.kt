package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.lastmile.clock.Clock

/** Moves seasonal redelivery notices through their life cycle and keeps a journal of what it did. */
data class SeasonalRedeliveryNoticeWorkflow(
    var store: SeasonalRedeliveryNoticeStore,
    var validator: SeasonalRedeliveryNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("seasonal redelivery notice $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalRedeliveryNoticeStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal redelivery notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalRedeliveryNoticeStatus.EXPIRED)
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

fun newSeasonalRedeliveryNoticeWorkflow(store: SeasonalRedeliveryNoticeStore, clock: Clock): SeasonalRedeliveryNoticeWorkflow {
    return SeasonalRedeliveryNoticeWorkflow(
        store = store,
        validator = defaultSeasonalRedeliveryNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
