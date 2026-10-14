package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.clock.Clock

/** Moves primary hold notices through their life cycle and keeps a journal of what it did. */
data class PrimaryHoldNoticeManager(
    var store: PrimaryHoldNoticeStore,
    var validator: PrimaryHoldNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryHoldNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary hold notice $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryHoldNoticeStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary hold notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryHoldNoticeStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("primary hold notice $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newPrimaryHoldNoticeManager(store: PrimaryHoldNoticeStore, clock: Clock): PrimaryHoldNoticeManager {
    return PrimaryHoldNoticeManager(
        store = store,
        validator = defaultPrimaryHoldNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryHoldNoticeEngine(),
    )
}
