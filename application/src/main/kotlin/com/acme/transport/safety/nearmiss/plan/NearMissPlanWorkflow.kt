package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves near miss plans through their life cycle and keeps a journal of what it did. */
data class NearMissPlanDesk(
    var store: NearMissPlanStore,
    var validator: NearMissPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("near miss plan $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissPlanStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissPlanStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("near miss plan $code not found")
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

fun newNearMissPlanDesk(store: NearMissPlanStore, clock: Clock, flags: Registry): NearMissPlanDesk {
    return NearMissPlanDesk(
        store = store,
        validator = defaultNearMissPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissPlanEngine(flags),
    )
}
