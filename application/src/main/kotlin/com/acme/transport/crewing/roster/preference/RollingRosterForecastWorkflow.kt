package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.clock.Clock

/** Moves rolling roster forecasts through their life cycle and keeps a journal of what it did. */
data class RollingRosterForecastService(
    var store: RollingRosterForecastStore,
    var validator: RollingRosterForecastValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingRosterForecastEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling roster forecast $code not found")
        validator.requireValid(item)
        item.moveTo(RollingRosterForecastStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling roster forecast $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingRosterForecastStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("rolling roster forecast $code not found")
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

fun newRollingRosterForecastService(store: RollingRosterForecastStore, clock: Clock): RollingRosterForecastService {
    return RollingRosterForecastService(
        store = store,
        validator = defaultRollingRosterForecastValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingRosterForecastEngine(),
    )
}
