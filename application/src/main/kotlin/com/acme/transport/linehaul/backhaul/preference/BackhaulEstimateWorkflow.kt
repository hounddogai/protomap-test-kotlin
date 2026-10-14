package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul estimates through their life cycle and keeps a journal of what it did. */
data class BackhaulEstimateManager(
    var store: BackhaulEstimateStore,
    var validator: BackhaulEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("backhaul estimate $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("backhaul estimate $code not found")
        val result = engine.compute(item)
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

fun newBackhaulEstimateManager(store: BackhaulEstimateStore, clock: Clock, flags: Registry): BackhaulEstimateManager {
    return BackhaulEstimateManager(
        store = store,
        validator = defaultBackhaulEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulEstimateEngine(flags),
    )
}
