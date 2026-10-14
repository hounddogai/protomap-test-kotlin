package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves accessorial slots through their life cycle and keeps a journal of what it did. */
data class AccessorialSlotDesk(
    var store: AccessorialSlotStore,
    var validator: AccessorialSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("accessorial slot $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialSlotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialSlotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("accessorial slot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newAccessorialSlotDesk(store: AccessorialSlotStore, clock: Clock, flags: Registry): AccessorialSlotDesk {
    return AccessorialSlotDesk(
        store = store,
        validator = defaultAccessorialSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialSlotEngine(flags),
    )
}
