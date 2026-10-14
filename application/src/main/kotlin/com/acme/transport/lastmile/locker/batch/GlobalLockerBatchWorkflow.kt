package com.acme.transport.lastmile.locker.batch

import com.acme.transport.lastmile.clock.Clock

/** Moves global locker batches through their life cycle and keeps a journal of what it did. */
data class GlobalLockerBatchDesk(
    var store: GlobalLockerBatchStore,
    var validator: GlobalLockerBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalLockerBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global locker batch $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalLockerBatchStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global locker batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalLockerBatchStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global locker batch $code not found")
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

fun newGlobalLockerBatchDesk(store: GlobalLockerBatchStore, clock: Clock): GlobalLockerBatchDesk {
    return GlobalLockerBatchDesk(
        store = store,
        validator = defaultGlobalLockerBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalLockerBatchEngine(),
    )
}
