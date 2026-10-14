package com.acme.transport.terminal.sortation.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation estimates through their life cycle and keeps a journal of what it did. */
data class SortationEstimateService(
    var store: SortationEstimateStore,
    var validator: SortationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("sortation estimate $code not found")
        validator.requireValid(item)
        item.moveTo(SortationEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("sortation estimate $code not found")
        val result = engine.price(item)
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

fun newSortationEstimateService(store: SortationEstimateStore, clock: Clock, flags: Registry): SortationEstimateService {
    return SortationEstimateService(
        store = store,
        validator = defaultSortationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationEstimateEngine(flags),
    )
}
