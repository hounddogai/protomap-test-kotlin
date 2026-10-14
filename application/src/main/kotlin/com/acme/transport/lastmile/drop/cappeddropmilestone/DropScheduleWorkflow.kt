package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.lastmile.clock.Clock

/** Moves drop schedules through their life cycle and keeps a journal of what it did. */
data class DropScheduleService(
    var store: DropScheduleStore,
    var validator: DropScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("drop schedule $code not found")
        validator.requireValid(item)
        item.moveTo(DropScheduleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("drop schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropScheduleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("drop schedule $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newDropScheduleService(store: DropScheduleStore, clock: Clock): DropScheduleService {
    return DropScheduleService(
        store = store,
        validator = defaultDropScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropScheduleEngine(),
    )
}
