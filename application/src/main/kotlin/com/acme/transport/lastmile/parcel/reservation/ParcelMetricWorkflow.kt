package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel metrics through their life cycle and keeps a journal of what it did. */
data class ParcelMetricCoordinator(
    var store: ParcelMetricStore,
    var validator: ParcelMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelMetricEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("parcel metric $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelMetricStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelMetricStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("parcel metric $code not found")
        val result = engine.run(item)
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

fun newParcelMetricCoordinator(store: ParcelMetricStore, clock: Clock): ParcelMetricCoordinator {
    return ParcelMetricCoordinator(
        store = store,
        validator = defaultParcelMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelMetricEngine(),
    )
}
