package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.lastmile.clock.Clock

/** Moves manual parcel windows through their life cycle and keeps a journal of what it did. */
data class ManualParcelWindowDesk(
    var store: ManualParcelWindowStore,
    var validator: ManualParcelWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("manual parcel window $code not found")
        validator.requireValid(item)
        item.moveTo(ManualParcelWindowStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual parcel window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualParcelWindowStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newManualParcelWindowDesk(store: ManualParcelWindowStore, clock: Clock): ManualParcelWindowDesk {
    return ManualParcelWindowDesk(
        store = store,
        validator = defaultManualParcelWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
