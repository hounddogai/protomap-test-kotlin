package com.acme.transport.lastmile.drop.preference

import com.acme.transport.lastmile.clock.Clock

/** Moves tiered drop thresholds through their life cycle and keeps a journal of what it did. */
data class TieredDropThresholdManager(
    var store: TieredDropThresholdStore,
    var validator: TieredDropThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("tiered drop threshold $code not found")
        validator.requireValid(item)
        item.moveTo(TieredDropThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered drop threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredDropThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newTieredDropThresholdManager(store: TieredDropThresholdStore, clock: Clock): TieredDropThresholdManager {
    return TieredDropThresholdManager(
        store = store,
        validator = defaultTieredDropThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
