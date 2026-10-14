package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves parcel summaries through their life cycle and keeps a journal of what it did. */
data class ParcelSummaryService(
    var store: ParcelSummaryStore,
    var validator: ParcelSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel summary $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("parcel summary $code not found")
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

fun newParcelSummaryService(store: ParcelSummaryStore, clock: Clock, flags: Registry): ParcelSummaryService {
    return ParcelSummaryService(
        store = store,
        validator = defaultParcelSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelSummaryEngine(flags),
    )
}
