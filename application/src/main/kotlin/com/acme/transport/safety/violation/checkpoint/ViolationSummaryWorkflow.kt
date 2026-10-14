package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation summaries through their life cycle and keeps a journal of what it did. */
data class ViolationSummaryManager(
    var store: ViolationSummaryStore,
    var validator: ViolationSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("violation summary $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationSummaryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("violation summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationSummaryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("violation summary $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newViolationSummaryManager(store: ViolationSummaryStore, clock: Clock, flags: Registry): ViolationSummaryManager {
    return ViolationSummaryManager(
        store = store,
        validator = defaultViolationSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationSummaryEngine(flags),
    )
}
