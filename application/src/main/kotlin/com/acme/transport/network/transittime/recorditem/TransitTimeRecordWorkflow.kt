package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves transit time records through their life cycle and keeps a journal of what it did. */
data class TransitTimeRecordManager(
    var store: TransitTimeRecordStore,
    var validator: TransitTimeRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TransitTimeRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("transit time record $code not found")
        validator.requireValid(item)
        item.moveTo(TransitTimeRecordStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("transit time record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TransitTimeRecordStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("transit time record $code not found")
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

fun newTransitTimeRecordManager(store: TransitTimeRecordStore, clock: Clock, flags: Registry): TransitTimeRecordManager {
    return TransitTimeRecordManager(
        store = store,
        validator = defaultTransitTimeRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTransitTimeRecordEngine(flags),
    )
}
