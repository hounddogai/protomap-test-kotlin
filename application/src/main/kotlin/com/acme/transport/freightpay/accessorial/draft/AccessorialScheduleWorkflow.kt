package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves accessorial schedules through their life cycle and keeps a journal of what it did. */
data class AccessorialScheduleDesk(
    var store: AccessorialScheduleStore,
    var validator: AccessorialScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("accessorial schedule $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialScheduleStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialScheduleStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("accessorial schedule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newAccessorialScheduleDesk(store: AccessorialScheduleStore, clock: Clock, flags: Registry): AccessorialScheduleDesk {
    return AccessorialScheduleDesk(
        store = store,
        validator = defaultAccessorialScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialScheduleEngine(flags),
    )
}
