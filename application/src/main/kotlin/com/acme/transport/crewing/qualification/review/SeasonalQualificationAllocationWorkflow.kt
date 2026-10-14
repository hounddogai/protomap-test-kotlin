package com.acme.transport.crewing.qualification.review

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/**
 * Moves seasonal qualification allocations through their life cycle and keeps a journal of what it did.
 */
data class SeasonalQualificationAllocationDesk(
    var store: SeasonalQualificationAllocationStore,
    var validator: SeasonalQualificationAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalQualificationAllocationEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal qualification allocation $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalQualificationAllocationStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal qualification allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalQualificationAllocationStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal qualification allocation $code not found")
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

fun newSeasonalQualificationAllocationDesk(store: SeasonalQualificationAllocationStore, clock: Clock, flags: Registry): SeasonalQualificationAllocationDesk {
    return SeasonalQualificationAllocationDesk(
        store = store,
        validator = defaultSeasonalQualificationAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalQualificationAllocationEngine(flags),
    )
}
