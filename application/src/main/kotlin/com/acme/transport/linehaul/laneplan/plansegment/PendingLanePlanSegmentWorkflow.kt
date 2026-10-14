package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves pending lane plan segments through their life cycle and keeps a journal of what it did. */
data class PendingLanePlanSegmentDesk(
    var store: PendingLanePlanSegmentStore,
    var validator: PendingLanePlanSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingLanePlanSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending lane plan segment $code not found")
        validator.requireValid(item)
        item.moveTo(PendingLanePlanSegmentStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("pending lane plan segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingLanePlanSegmentStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending lane plan segment $code not found")
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

fun newPendingLanePlanSegmentDesk(store: PendingLanePlanSegmentStore, clock: Clock, flags: Registry): PendingLanePlanSegmentDesk {
    return PendingLanePlanSegmentDesk(
        store = store,
        validator = defaultPendingLanePlanSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingLanePlanSegmentEngine(flags),
    )
}
