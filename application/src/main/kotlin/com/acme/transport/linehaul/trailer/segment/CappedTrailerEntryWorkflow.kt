package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capped trailer entries through their life cycle and keeps a journal of what it did. */
data class CappedTrailerEntryService(
    var store: CappedTrailerEntryStore,
    var validator: CappedTrailerEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTrailerEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped trailer entry $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped trailer entry $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newCappedTrailerEntryService(store: CappedTrailerEntryStore, clock: Clock, flags: Registry): CappedTrailerEntryService {
    return CappedTrailerEntryService(
        store = store,
        validator = defaultCappedTrailerEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTrailerEntryEngine(flags),
    )
}
