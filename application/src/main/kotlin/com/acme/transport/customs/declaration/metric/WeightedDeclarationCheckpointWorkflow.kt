package com.acme.transport.customs.declaration.metric

import com.acme.transport.customs.clock.Clock

/**
 * Moves weighted declaration checkpoints through their life cycle and keeps a journal of what it did.
 */
data class WeightedDeclarationCheckpointCoordinator(
    var store: WeightedDeclarationCheckpointStore,
    var validator: WeightedDeclarationCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted declaration checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDeclarationCheckpointStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted declaration checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDeclarationCheckpointStatus.ARCHIVED)
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

fun newWeightedDeclarationCheckpointCoordinator(store: WeightedDeclarationCheckpointStore, clock: Clock): WeightedDeclarationCheckpointCoordinator {
    return WeightedDeclarationCheckpointCoordinator(
        store = store,
        validator = defaultWeightedDeclarationCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
