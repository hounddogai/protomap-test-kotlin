package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves global relay variances through their life cycle and keeps a journal of what it did. */
data class GlobalRelayVarianceDesk(
    var store: GlobalRelayVarianceStore,
    var validator: GlobalRelayVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRelayVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("global relay variance $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRelayVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global relay variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRelayVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global relay variance $code not found")
        val result = engine.price(item)
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

fun newGlobalRelayVarianceDesk(store: GlobalRelayVarianceStore, clock: Clock, flags: Registry): GlobalRelayVarianceDesk {
    return GlobalRelayVarianceDesk(
        store = store,
        validator = defaultGlobalRelayVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRelayVarianceEngine(flags),
    )
}
