package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves tire milestones through their life cycle and keeps a journal of what it did. */
data class TireMilestoneService(
    var store: TireMilestoneStore,
    var validator: TireMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TireMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("tire milestone $code not found")
        validator.requireValid(item)
        item.moveTo(TireMilestoneStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("tire milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TireMilestoneStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tire milestone $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newTireMilestoneService(store: TireMilestoneStore, clock: Clock, flags: Registry): TireMilestoneService {
    return TireMilestoneService(
        store = store,
        validator = defaultTireMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTireMilestoneEngine(flags),
    )
}
