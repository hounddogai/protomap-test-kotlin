package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel windows through their life cycle and keeps a journal of what it did. */
data class ParcelWindowService(
    var store: ParcelWindowStore,
    var validator: ParcelWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("parcel window $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelWindowStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelWindowStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("parcel window $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newParcelWindowService(store: ParcelWindowStore, clock: Clock): ParcelWindowService {
    return ParcelWindowService(
        store = store,
        validator = defaultParcelWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelWindowEngine(),
    )
}
