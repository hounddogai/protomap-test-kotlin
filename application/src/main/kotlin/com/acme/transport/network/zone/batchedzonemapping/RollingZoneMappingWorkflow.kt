package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves rolling zone mappings through their life cycle and keeps a journal of what it did. */
data class RollingZoneMappingDesk(
    var store: RollingZoneMappingStore,
    var validator: RollingZoneMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingZoneMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling zone mapping $code not found")
        validator.requireValid(item)
        item.moveTo(RollingZoneMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling zone mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingZoneMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling zone mapping $code not found")
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

fun newRollingZoneMappingDesk(store: RollingZoneMappingStore, clock: Clock, flags: Registry): RollingZoneMappingDesk {
    return RollingZoneMappingDesk(
        store = store,
        validator = defaultRollingZoneMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingZoneMappingEngine(flags),
    )
}
