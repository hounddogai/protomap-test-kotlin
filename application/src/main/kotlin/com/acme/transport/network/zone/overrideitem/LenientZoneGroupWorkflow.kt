package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves lenient zone groups through their life cycle and keeps a journal of what it did. */
data class LenientZoneGroupDesk(
    var store: LenientZoneGroupStore,
    var validator: LenientZoneGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientZoneGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient zone group $code not found")
        validator.requireValid(item)
        item.moveTo(LenientZoneGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient zone group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientZoneGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient zone group $code not found")
        val result = engine.compute(item)
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

fun newLenientZoneGroupDesk(store: LenientZoneGroupStore, clock: Clock, flags: Registry): LenientZoneGroupDesk {
    return LenientZoneGroupDesk(
        store = store,
        validator = defaultLenientZoneGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientZoneGroupEngine(flags),
    )
}
