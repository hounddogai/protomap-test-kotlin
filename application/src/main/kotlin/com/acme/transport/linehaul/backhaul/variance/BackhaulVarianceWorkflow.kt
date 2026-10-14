package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul variances through their life cycle and keeps a journal of what it did. */
data class BackhaulVarianceDesk(
    var store: BackhaulVarianceStore,
    var validator: BackhaulVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("backhaul variance $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("backhaul variance $code not found")
        val result = engine.evaluate(item)
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

fun newBackhaulVarianceDesk(store: BackhaulVarianceStore, clock: Clock, flags: Registry): BackhaulVarianceDesk {
    return BackhaulVarianceDesk(
        store = store,
        validator = defaultBackhaulVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulVarianceEngine(flags),
    )
}
