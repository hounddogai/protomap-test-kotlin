package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves regional violation digests through their life cycle and keeps a journal of what it did. */
data class RegionalViolationDigestManager(
    var store: RegionalViolationDigestStore,
    var validator: RegionalViolationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalViolationDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional violation digest $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalViolationDigestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional violation digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalViolationDigestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional violation digest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newRegionalViolationDigestManager(store: RegionalViolationDigestStore, clock: Clock, flags: Registry): RegionalViolationDigestManager {
    return RegionalViolationDigestManager(
        store = store,
        validator = defaultRegionalViolationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalViolationDigestEngine(flags),
    )
}
