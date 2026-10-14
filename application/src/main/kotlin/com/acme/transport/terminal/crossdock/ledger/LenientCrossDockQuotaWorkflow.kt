package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.terminal.clock.Clock

/** Moves lenient cross dock quotas through their life cycle and keeps a journal of what it did. */
data class LenientCrossDockQuotaManager(
    var store: LenientCrossDockQuotaStore,
    var validator: LenientCrossDockQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCrossDockQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient cross dock quota $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCrossDockQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient cross dock quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCrossDockQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient cross dock quota $code not found")
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

fun newLenientCrossDockQuotaManager(store: LenientCrossDockQuotaStore, clock: Clock): LenientCrossDockQuotaManager {
    return LenientCrossDockQuotaManager(
        store = store,
        validator = defaultLenientCrossDockQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCrossDockQuotaEngine(),
    )
}
