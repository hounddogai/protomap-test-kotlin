package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves parcel exceptions through their life cycle and keeps a journal of what it did. */
data class ParcelExceptionManager(
    var store: ParcelExceptionStore,
    var validator: ParcelExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("parcel exception $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelExceptionStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("parcel exception $code not found")
        val result = engine.compute(item)
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

fun newParcelExceptionManager(store: ParcelExceptionStore, clock: Clock, flags: Registry): ParcelExceptionManager {
    return ParcelExceptionManager(
        store = store,
        validator = defaultParcelExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelExceptionEngine(flags),
    )
}
