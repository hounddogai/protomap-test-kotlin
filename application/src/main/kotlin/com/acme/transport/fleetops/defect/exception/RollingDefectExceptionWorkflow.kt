package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling defect exceptions through their life cycle and keeps a journal of what it did. */
data class RollingDefectExceptionDesk(
    var store: RollingDefectExceptionStore,
    var validator: RollingDefectExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDefectExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling defect exception $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDefectExceptionStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling defect exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDefectExceptionStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling defect exception $code not found")
        val result = engine.price(item)
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

fun newRollingDefectExceptionDesk(store: RollingDefectExceptionStore, clock: Clock, flags: Registry): RollingDefectExceptionDesk {
    return RollingDefectExceptionDesk(
        store = store,
        validator = defaultRollingDefectExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDefectExceptionEngine(flags),
    )
}
