package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.clock.Clock

/** Moves lenient violation assignments through their life cycle and keeps a journal of what it did. */
data class LenientViolationAssignmentManager(
    var store: LenientViolationAssignmentStore,
    var validator: LenientViolationAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientViolationAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient violation assignment $code not found")
        validator.requireValid(item)
        item.moveTo(LenientViolationAssignmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient violation assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientViolationAssignmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient violation assignment $code not found")
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

fun newLenientViolationAssignmentManager(store: LenientViolationAssignmentStore, clock: Clock): LenientViolationAssignmentManager {
    return LenientViolationAssignmentManager(
        store = store,
        validator = defaultLenientViolationAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientViolationAssignmentEngine(),
    )
}
