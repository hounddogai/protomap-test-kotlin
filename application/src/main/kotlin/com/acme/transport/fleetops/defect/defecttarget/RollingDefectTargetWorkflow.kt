package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling defect targets through their life cycle and keeps a journal of what it did. */
data class RollingDefectTargetDesk(
    var store: RollingDefectTargetStore,
    var validator: RollingDefectTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDefectTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling defect target $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDefectTargetStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling defect target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDefectTargetStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling defect target $code not found")
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

fun newRollingDefectTargetDesk(store: RollingDefectTargetStore, clock: Clock, flags: Registry): RollingDefectTargetDesk {
    return RollingDefectTargetDesk(
        store = store,
        validator = defaultRollingDefectTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDefectTargetEngine(flags),
    )
}
