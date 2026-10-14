package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy rest break estimates through their life cycle and keeps a journal of what it did. */
data class LegacyRestBreakEstimateService(
    var store: LegacyRestBreakEstimateStore,
    var validator: LegacyRestBreakEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyRestBreakEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy rest break estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyRestBreakEstimateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy rest break estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyRestBreakEstimateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy rest break estimate $code not found")
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

fun newLegacyRestBreakEstimateService(store: LegacyRestBreakEstimateStore, clock: Clock, flags: Registry): LegacyRestBreakEstimateService {
    return LegacyRestBreakEstimateService(
        store = store,
        validator = defaultLegacyRestBreakEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyRestBreakEstimateEngine(flags),
    )
}
