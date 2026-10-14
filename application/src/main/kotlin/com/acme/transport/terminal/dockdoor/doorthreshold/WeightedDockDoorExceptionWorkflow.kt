package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.terminal.clock.Clock

/** Moves weighted dock door exceptions through their life cycle and keeps a journal of what it did. */
data class WeightedDockDoorExceptionCoordinator(
    var store: WeightedDockDoorExceptionStore,
    var validator: WeightedDockDoorExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedDockDoorExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("weighted dock door exception $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedDockDoorExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted dock door exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedDockDoorExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("weighted dock door exception $code not found")
        val result = engine.compute(item)
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

fun newWeightedDockDoorExceptionCoordinator(store: WeightedDockDoorExceptionStore, clock: Clock): WeightedDockDoorExceptionCoordinator {
    return WeightedDockDoorExceptionCoordinator(
        store = store,
        validator = defaultWeightedDockDoorExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedDockDoorExceptionEngine(),
    )
}
