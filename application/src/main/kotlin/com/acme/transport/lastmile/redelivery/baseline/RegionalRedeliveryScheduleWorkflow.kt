package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves regional redelivery schedules through their life cycle and keeps a journal of what it did. */
data class RegionalRedeliveryScheduleService(
    var store: RegionalRedeliveryScheduleStore,
    var validator: RegionalRedeliveryScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalRedeliveryScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional redelivery schedule $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRedeliveryScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional redelivery schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRedeliveryScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional redelivery schedule $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newRegionalRedeliveryScheduleService(store: RegionalRedeliveryScheduleStore, clock: Clock, flags: Registry): RegionalRedeliveryScheduleService {
    return RegionalRedeliveryScheduleService(
        store = store,
        validator = defaultRegionalRedeliveryScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalRedeliveryScheduleEngine(flags),
    )
}
