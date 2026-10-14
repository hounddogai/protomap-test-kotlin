package com.acme.transport.safety.violation.snapshot

import com.acme.transport.safety.clock.Clock

/** Moves capped violation thresholds through their life cycle and keeps a journal of what it did. */
data class CappedViolationThresholdDesk(
    var store: CappedViolationThresholdStore,
    var validator: CappedViolationThresholdValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedViolationThresholdEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped violation threshold $code not found")
        validator.requireValid(item)
        item.moveTo(CappedViolationThresholdStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped violation threshold $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedViolationThresholdStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped violation threshold $code not found")
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

fun newCappedViolationThresholdDesk(store: CappedViolationThresholdStore, clock: Clock): CappedViolationThresholdDesk {
    return CappedViolationThresholdDesk(
        store = store,
        validator = defaultCappedViolationThresholdValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedViolationThresholdEngine(),
    )
}
