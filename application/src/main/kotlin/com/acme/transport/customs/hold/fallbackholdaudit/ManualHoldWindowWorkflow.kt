package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual hold windows through their life cycle and keeps a journal of what it did. */
data class ManualHoldWindowManager(
    var store: ManualHoldWindowStore,
    var validator: ManualHoldWindowValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualHoldWindowEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual hold window $code not found")
        validator.requireValid(item)
        item.moveTo(ManualHoldWindowStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual hold window $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualHoldWindowStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("manual hold window $code not found")
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

fun newManualHoldWindowManager(store: ManualHoldWindowStore, clock: Clock, flags: Registry): ManualHoldWindowManager {
    return ManualHoldWindowManager(
        store = store,
        validator = defaultManualHoldWindowValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualHoldWindowEngine(flags),
    )
}
