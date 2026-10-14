package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.clock.Clock

/** Moves lenient duty period profiles through their life cycle and keeps a journal of what it did. */
data class LenientDutyPeriodProfileCoordinator(
    var store: LenientDutyPeriodProfileStore,
    var validator: LenientDutyPeriodProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDutyPeriodProfileEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient duty period profile $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDutyPeriodProfileStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient duty period profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDutyPeriodProfileStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient duty period profile $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLenientDutyPeriodProfileCoordinator(store: LenientDutyPeriodProfileStore, clock: Clock): LenientDutyPeriodProfileCoordinator {
    return LenientDutyPeriodProfileCoordinator(
        store = store,
        validator = defaultLenientDutyPeriodProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDutyPeriodProfileEngine(),
    )
}
