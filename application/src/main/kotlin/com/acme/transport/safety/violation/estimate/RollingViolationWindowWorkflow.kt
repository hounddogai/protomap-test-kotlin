package com.acme.transport.safety.violation.estimate

import com.acme.transport.safety.clock.Clock

/** Moves rolling violation windows through their life cycle and keeps a journal of what it did. */
data class RollingViolationWindowManager(
    var store: RollingViolationWindowStore,
    var validator: RollingViolationWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingViolationWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling violation window $code not found")
        validator.requireValid(item)
        item.moveTo(RollingViolationWindowStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling violation window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingViolationWindowStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling violation window $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newRollingViolationWindowManager(store: RollingViolationWindowStore, clock: Clock): RollingViolationWindowManager {
    return RollingViolationWindowManager(
        store = store,
        validator = defaultRollingViolationWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingViolationWindowEngine(),
    )
}
