package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.clock.Clock

/** Moves broker notices through their life cycle and keeps a journal of what it did. */
data class BrokerNoticeWorkflow(
    var store: BrokerNoticeStore,
    var validator: BrokerNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("broker notice $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerNoticeStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("broker notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerNoticeStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("broker notice $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newBrokerNoticeWorkflow(store: BrokerNoticeStore, clock: Clock): BrokerNoticeWorkflow {
    return BrokerNoticeWorkflow(
        store = store,
        validator = defaultBrokerNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerNoticeEngine(),
    )
}
