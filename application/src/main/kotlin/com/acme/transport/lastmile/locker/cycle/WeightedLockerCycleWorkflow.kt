package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.lastmile.clock.Clock

/** Moves weighted locker cycles through their life cycle and keeps a journal of what it did. */
data class WeightedLockerCycleService(
    var store: WeightedLockerCycleStore,
    var validator: WeightedLockerCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: WeightedLockerCycleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("weighted locker cycle $code not found")
        validator.requireValid(item)
        item.moveTo(WeightedLockerCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("weighted locker cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(WeightedLockerCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("weighted locker cycle $code not found")
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

fun newWeightedLockerCycleService(store: WeightedLockerCycleStore, clock: Clock): WeightedLockerCycleService {
    return WeightedLockerCycleService(
        store = store,
        validator = defaultWeightedLockerCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultWeightedLockerCycleEngine(),
    )
}
