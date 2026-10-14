package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.crewing.clock.Clock

/** Moves duty period groups through their life cycle and keeps a journal of what it did. */
data class DutyPeriodGroupManager(
    var store: DutyPeriodGroupStore,
    var validator: DutyPeriodGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DutyPeriodGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("duty period group $code not found")
        validator.requireValid(item)
        item.moveTo(DutyPeriodGroupStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("duty period group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DutyPeriodGroupStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("duty period group $code not found")
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

fun newDutyPeriodGroupManager(store: DutyPeriodGroupStore, clock: Clock): DutyPeriodGroupManager {
    return DutyPeriodGroupManager(
        store = store,
        validator = defaultDutyPeriodGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDutyPeriodGroupEngine(),
    )
}
