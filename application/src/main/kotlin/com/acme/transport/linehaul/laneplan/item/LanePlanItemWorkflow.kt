package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan items through their life cycle and keeps a journal of what it did. */
data class LanePlanItemDesk(
    var store: LanePlanItemStore,
    var validator: LanePlanItemValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanItemEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lane plan item $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanItemStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan item $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanItemStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lane plan item $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newLanePlanItemDesk(store: LanePlanItemStore, clock: Clock, flags: Registry): LanePlanItemDesk {
    return LanePlanItemDesk(
        store = store,
        validator = defaultLanePlanItemValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanItemEngine(flags),
    )
}
