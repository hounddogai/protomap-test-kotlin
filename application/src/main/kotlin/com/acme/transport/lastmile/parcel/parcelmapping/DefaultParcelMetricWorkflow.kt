package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.lastmile.clock.Clock

/** Moves default parcel metrics through their life cycle and keeps a journal of what it did. */
data class DefaultParcelMetricCoordinator(
    var store: DefaultParcelMetricStore,
    var validator: DefaultParcelMetricValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default parcel metric $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultParcelMetricStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default parcel metric $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultParcelMetricStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newDefaultParcelMetricCoordinator(store: DefaultParcelMetricStore, clock: Clock): DefaultParcelMetricCoordinator {
    return DefaultParcelMetricCoordinator(
        store = store,
        validator = defaultDefaultParcelMetricValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
