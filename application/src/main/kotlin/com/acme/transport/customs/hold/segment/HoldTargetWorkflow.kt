package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves hold targets through their life cycle and keeps a journal of what it did. */
data class HoldTargetDesk(
    var store: HoldTargetStore,
    var validator: HoldTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("hold target $code not found")
        validator.requireValid(item)
        item.moveTo(HoldTargetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("hold target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldTargetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("hold target $code not found")
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

fun newHoldTargetDesk(store: HoldTargetStore, clock: Clock, flags: Registry): HoldTargetDesk {
    return HoldTargetDesk(
        store = store,
        validator = defaultHoldTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldTargetEngine(flags),
    )
}
