package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.clock.Clock

/** Moves drop thresholds through their life cycle and keeps a journal of what it did. */
data class DropThresholdManager(
    var store: DropThresholdStore,
    var validator: DropThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("drop threshold $code not found")
        validator.requireValid(item)
        item.moveTo(DropThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("drop threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("drop threshold $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newDropThresholdManager(store: DropThresholdStore, clock: Clock): DropThresholdManager {
    return DropThresholdManager(
        store = store,
        validator = defaultDropThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropThresholdEngine(),
    )
}
