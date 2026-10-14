package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.clock.Clock

/** Moves capped accessorial entries through their life cycle and keeps a journal of what it did. */
data class CappedAccessorialEntryWorkflow(
    var store: CappedAccessorialEntryStore,
    var validator: CappedAccessorialEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedAccessorialEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped accessorial entry $code not found")
        validator.requireValid(item)
        item.moveTo(CappedAccessorialEntryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped accessorial entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedAccessorialEntryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped accessorial entry $code not found")
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

fun newCappedAccessorialEntryWorkflow(store: CappedAccessorialEntryStore, clock: Clock): CappedAccessorialEntryWorkflow {
    return CappedAccessorialEntryWorkflow(
        store = store,
        validator = defaultCappedAccessorialEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedAccessorialEntryEngine(),
    )
}
