package com.acme.transport.safety.violation.forecast

import com.acme.transport.safety.clock.Clock

/** Moves capped violation mappings through their life cycle and keeps a journal of what it did. */
data class CappedViolationMappingManager(
    var store: CappedViolationMappingStore,
    var validator: CappedViolationMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedViolationMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped violation mapping $code not found")
        validator.requireValid(item)
        item.moveTo(CappedViolationMappingStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped violation mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedViolationMappingStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped violation mapping $code not found")
        val result = engine.compute(item)
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

fun newCappedViolationMappingManager(store: CappedViolationMappingStore, clock: Clock): CappedViolationMappingManager {
    return CappedViolationMappingManager(
        store = store,
        validator = defaultCappedViolationMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedViolationMappingEngine(),
    )
}
