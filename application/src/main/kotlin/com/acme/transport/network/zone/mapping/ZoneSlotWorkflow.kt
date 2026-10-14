package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves zone slots through their life cycle and keeps a journal of what it did. */
data class ZoneSlotDesk(
    var store: ZoneSlotStore,
    var validator: ZoneSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("zone slot $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneSlotStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("zone slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneSlotStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("zone slot $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newZoneSlotDesk(store: ZoneSlotStore, clock: Clock, flags: Registry): ZoneSlotDesk {
    return ZoneSlotDesk(
        store = store,
        validator = defaultZoneSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneSlotEngine(flags),
    )
}
