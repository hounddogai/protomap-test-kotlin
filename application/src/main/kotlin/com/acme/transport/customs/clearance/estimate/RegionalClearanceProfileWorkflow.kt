package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves regional clearance profiles through their life cycle and keeps a journal of what it did. */
data class RegionalClearanceProfileService(
    var store: RegionalClearanceProfileStore,
    var validator: RegionalClearanceProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalClearanceProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("regional clearance profile $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalClearanceProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional clearance profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalClearanceProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional clearance profile $code not found")
        val result = engine.price(item)
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

fun newRegionalClearanceProfileService(store: RegionalClearanceProfileStore, clock: Clock, flags: Registry): RegionalClearanceProfileService {
    return RegionalClearanceProfileService(
        store = store,
        validator = defaultRegionalClearanceProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalClearanceProfileEngine(flags),
    )
}
