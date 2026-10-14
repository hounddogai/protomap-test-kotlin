package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel estimates through their life cycle and keeps a journal of what it did. */
data class ParcelEstimateCoordinator(
    var store: ParcelEstimateStore,
    var validator: ParcelEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel estimate $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("parcel estimate $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newParcelEstimateCoordinator(store: ParcelEstimateStore, clock: Clock): ParcelEstimateCoordinator {
    return ParcelEstimateCoordinator(
        store = store,
        validator = defaultParcelEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelEstimateEngine(),
    )
}
