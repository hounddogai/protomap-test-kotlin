package com.acme.transport.safety.violation.entry

import com.acme.transport.safety.clock.Clock

/** Moves manual violation windows through their life cycle and keeps a journal of what it did. */
data class ManualViolationWindowService(
    var store: ManualViolationWindowStore,
    var validator: ManualViolationWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualViolationWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("manual violation window $code not found")
        validator.requireValid(item)
        item.moveTo(ManualViolationWindowStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual violation window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualViolationWindowStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual violation window $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newManualViolationWindowService(store: ManualViolationWindowStore, clock: Clock): ManualViolationWindowService {
    return ManualViolationWindowService(
        store = store,
        validator = defaultManualViolationWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualViolationWindowEngine(),
    )
}
