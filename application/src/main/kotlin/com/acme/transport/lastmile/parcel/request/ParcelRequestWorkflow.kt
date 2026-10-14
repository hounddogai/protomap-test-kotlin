package com.acme.transport.lastmile.parcel.request

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel requests through their life cycle and keeps a journal of what it did. */
data class ParcelRequestService(
    var store: ParcelRequestStore,
    var validator: ParcelRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelRequestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel request $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelRequestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelRequestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("parcel request $code not found")
        val result = engine.evaluate(item)
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

fun newParcelRequestService(store: ParcelRequestStore, clock: Clock): ParcelRequestService {
    return ParcelRequestService(
        store = store,
        validator = defaultParcelRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelRequestEngine(),
    )
}
