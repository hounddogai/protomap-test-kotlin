package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel variances through their life cycle and keeps a journal of what it did. */
data class ParcelVarianceCoordinator(
    var store: ParcelVarianceStore,
    var validator: ParcelVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("parcel variance $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("parcel variance $code not found")
        val result = engine.price(item)
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

fun newParcelVarianceCoordinator(store: ParcelVarianceStore, clock: Clock): ParcelVarianceCoordinator {
    return ParcelVarianceCoordinator(
        store = store,
        validator = defaultParcelVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelVarianceEngine(),
    )
}
