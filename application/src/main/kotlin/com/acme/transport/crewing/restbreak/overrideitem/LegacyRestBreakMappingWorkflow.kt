package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.clock.Clock

/** Moves legacy rest break mappings through their life cycle and keeps a journal of what it did. */
data class LegacyRestBreakMappingCoordinator(
    var store: LegacyRestBreakMappingStore,
    var validator: LegacyRestBreakMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyRestBreakMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy rest break mapping $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyRestBreakMappingStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy rest break mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyRestBreakMappingStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy rest break mapping $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newLegacyRestBreakMappingCoordinator(store: LegacyRestBreakMappingStore, clock: Clock): LegacyRestBreakMappingCoordinator {
    return LegacyRestBreakMappingCoordinator(
        store = store,
        validator = defaultLegacyRestBreakMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyRestBreakMappingEngine(),
    )
}
