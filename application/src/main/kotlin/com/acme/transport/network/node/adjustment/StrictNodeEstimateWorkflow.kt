package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves strict node estimates through their life cycle and keeps a journal of what it did. */
data class StrictNodeEstimateManager(
    var store: StrictNodeEstimateStore,
    var validator: StrictNodeEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictNodeEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict node estimate $code not found")
        validator.requireValid(item)
        item.moveTo(StrictNodeEstimateStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict node estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictNodeEstimateStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict node estimate $code not found")
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

fun newStrictNodeEstimateManager(store: StrictNodeEstimateStore, clock: Clock, flags: Registry): StrictNodeEstimateManager {
    return StrictNodeEstimateManager(
        store = store,
        validator = defaultStrictNodeEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictNodeEstimateEngine(flags),
    )
}
