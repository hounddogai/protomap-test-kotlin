package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves violation adjustments through their life cycle and keeps a journal of what it did. */
data class ViolationAdjustmentManager(
    var store: ViolationAdjustmentStore,
    var validator: ViolationAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ViolationAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("violation adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ViolationAdjustmentStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("violation adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ViolationAdjustmentStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("violation adjustment $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newViolationAdjustmentManager(store: ViolationAdjustmentStore, clock: Clock, flags: Registry): ViolationAdjustmentManager {
    return ViolationAdjustmentManager(
        store = store,
        validator = defaultViolationAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultViolationAdjustmentEngine(flags),
    )
}
