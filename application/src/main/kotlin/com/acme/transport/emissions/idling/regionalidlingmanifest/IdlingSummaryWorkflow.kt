package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves idling summaries through their life cycle and keeps a journal of what it did. */
data class IdlingSummaryService(
    var store: IdlingSummaryStore,
    var validator: IdlingSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling summary $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("idling summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("idling summary $code not found")
        val result = engine.run(item)
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

fun newIdlingSummaryService(store: IdlingSummaryStore, clock: Clock, flags: Registry): IdlingSummaryService {
    return IdlingSummaryService(
        store = store,
        validator = defaultIdlingSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingSummaryEngine(flags),
    )
}
