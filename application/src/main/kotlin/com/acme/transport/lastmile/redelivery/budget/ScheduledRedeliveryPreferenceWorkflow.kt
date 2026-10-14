package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves scheduled redelivery preferences through their life cycle and keeps a journal of what it did.
 */
data class ScheduledRedeliveryPreferenceService(
    var store: ScheduledRedeliveryPreferenceStore,
    var validator: ScheduledRedeliveryPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledRedeliveryPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled redelivery preference $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledRedeliveryPreferenceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled redelivery preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledRedeliveryPreferenceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled redelivery preference $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledRedeliveryPreferenceService(store: ScheduledRedeliveryPreferenceStore, clock: Clock, flags: Registry): ScheduledRedeliveryPreferenceService {
    return ScheduledRedeliveryPreferenceService(
        store = store,
        validator = defaultScheduledRedeliveryPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledRedeliveryPreferenceEngine(flags),
    )
}
