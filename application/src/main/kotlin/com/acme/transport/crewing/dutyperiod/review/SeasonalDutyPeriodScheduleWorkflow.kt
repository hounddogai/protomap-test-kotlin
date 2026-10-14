package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal duty period schedules through their life cycle and keeps a journal of what it did.
 */
data class SeasonalDutyPeriodScheduleService(
    var store: SeasonalDutyPeriodScheduleStore,
    var validator: SeasonalDutyPeriodScheduleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalDutyPeriodScheduleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("seasonal duty period schedule $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDutyPeriodScheduleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal duty period schedule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDutyPeriodScheduleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal duty period schedule $code not found")
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

fun newSeasonalDutyPeriodScheduleService(store: SeasonalDutyPeriodScheduleStore, clock: Clock, flags: Registry): SeasonalDutyPeriodScheduleService {
    return SeasonalDutyPeriodScheduleService(
        store = store,
        validator = defaultSeasonalDutyPeriodScheduleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalDutyPeriodScheduleEngine(flags),
    )
}
