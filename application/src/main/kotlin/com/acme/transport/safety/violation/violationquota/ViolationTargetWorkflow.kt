package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation targets through their life cycle and keeps a journal of what it did. */
data class ViolationTargetManager(
    var store: ViolationTargetStore,
    var validator: ViolationTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("violation target $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationTargetStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("violation target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationTargetStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("violation target $code not found")
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

fun newViolationTargetManager(store: ViolationTargetStore, clock: Clock, flags: Registry): ViolationTargetManager {
    return ViolationTargetManager(
        store = store,
        validator = defaultViolationTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationTargetEngine(flags),
    )
}
