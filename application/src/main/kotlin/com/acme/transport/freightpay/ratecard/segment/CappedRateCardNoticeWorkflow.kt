package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped rate card notices through their life cycle and keeps a journal of what it did. */
data class CappedRateCardNoticeService(
    var store: CappedRateCardNoticeStore,
    var validator: CappedRateCardNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedRateCardNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped rate card notice $code not found")
        validator.requireValid(item)
        item.moveTo(CappedRateCardNoticeStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped rate card notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedRateCardNoticeStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped rate card notice $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newCappedRateCardNoticeService(store: CappedRateCardNoticeStore, clock: Clock, flags: Registry): CappedRateCardNoticeService {
    return CappedRateCardNoticeService(
        store = store,
        validator = defaultCappedRateCardNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedRateCardNoticeEngine(flags),
    )
}
