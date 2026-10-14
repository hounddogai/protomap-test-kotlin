package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves relay notices through their life cycle and keeps a journal of what it did. */
data class RelayNoticeManager(
    var store: RelayNoticeStore,
    var validator: RelayNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RelayNoticeEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("relay notice $code not found")
        validator.requireValid(item)
        item.moveTo(RelayNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("relay notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RelayNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("relay notice $code not found")
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

fun newRelayNoticeManager(store: RelayNoticeStore, clock: Clock, flags: Registry): RelayNoticeManager {
    return RelayNoticeManager(
        store = store,
        validator = defaultRelayNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRelayNoticeEngine(flags),
    )
}
