package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation entries through their life cycle and keeps a journal of what it did. */
data class ViolationEntryCoordinator(
    var store: ViolationEntryStore,
    var validator: ViolationEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("violation entry $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("violation entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("violation entry $code not found")
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

fun newViolationEntryCoordinator(store: ViolationEntryStore, clock: Clock, flags: Registry): ViolationEntryCoordinator {
    return ViolationEntryCoordinator(
        store = store,
        validator = defaultViolationEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationEntryEngine(flags),
    )
}
