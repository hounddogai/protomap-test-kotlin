package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel profiles through their life cycle and keeps a journal of what it did. */
data class ParcelProfileCoordinator(
    var store: ParcelProfileStore,
    var validator: ParcelProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel profile $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelProfileStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelProfileStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("parcel profile $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newParcelProfileCoordinator(store: ParcelProfileStore, clock: Clock): ParcelProfileCoordinator {
    return ParcelProfileCoordinator(
        store = store,
        validator = defaultParcelProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelProfileEngine(),
    )
}
