package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.safety.clock.Clock

/** Moves strict violation quotas through their life cycle and keeps a journal of what it did. */
data class StrictViolationQuotaDesk(
    var store: StrictViolationQuotaStore,
    var validator: StrictViolationQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictViolationQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict violation quota $code not found")
        validator.requireValid(item)
        item.moveTo(StrictViolationQuotaStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict violation quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictViolationQuotaStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict violation quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newStrictViolationQuotaDesk(store: StrictViolationQuotaStore, clock: Clock, flags: Registry): StrictViolationQuotaDesk {
    return StrictViolationQuotaDesk(
        store = store,
        validator = defaultStrictViolationQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictViolationQuotaEngine(flags),
    )
}
