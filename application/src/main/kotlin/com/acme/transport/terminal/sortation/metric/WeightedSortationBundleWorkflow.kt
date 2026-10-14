package com.acme.transport.terminal.sortation.metric

import com.acme.transport.terminal.clock.Clock

/** Moves weighted sortation bundles through their life cycle and keeps a journal of what it did. */
data class WeightedSortationBundleDesk(
    var store: WeightedSortationBundleStore,
    var validator: WeightedSortationBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted sortation bundle $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedSortationBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted sortation bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedSortationBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newWeightedSortationBundleDesk(store: WeightedSortationBundleStore, clock: Clock): WeightedSortationBundleDesk {
    return WeightedSortationBundleDesk(
        store = store,
        validator = defaultWeightedSortationBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
