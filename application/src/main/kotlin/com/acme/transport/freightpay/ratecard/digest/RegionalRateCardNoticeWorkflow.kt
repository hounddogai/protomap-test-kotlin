package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.clock.Clock

/** Moves regional rate card notices through their life cycle and keeps a journal of what it did. */
data class RegionalRateCardNoticeCoordinator(
    var store: RegionalRateCardNoticeStore,
    var validator: RegionalRateCardNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional rate card notice $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRateCardNoticeStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional rate card notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRateCardNoticeStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newRegionalRateCardNoticeCoordinator(store: RegionalRateCardNoticeStore, clock: Clock): RegionalRateCardNoticeCoordinator {
    return RegionalRateCardNoticeCoordinator(
        store = store,
        validator = defaultRegionalRateCardNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
