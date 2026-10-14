package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global rate card records through their life cycle and keeps a journal of what it did. */
data class GlobalRateCardRecordWorkflow(
    var store: GlobalRateCardRecordStore,
    var validator: GlobalRateCardRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRateCardRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global rate card record $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRateCardRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global rate card record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRateCardRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global rate card record $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newGlobalRateCardRecordWorkflow(store: GlobalRateCardRecordStore, clock: Clock, flags: Registry): GlobalRateCardRecordWorkflow {
    return GlobalRateCardRecordWorkflow(
        store = store,
        validator = defaultGlobalRateCardRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRateCardRecordEngine(flags),
    )
}
