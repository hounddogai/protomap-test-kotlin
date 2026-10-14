package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation tiers through their life cycle and keeps a journal of what it did. */
data class SortationTierWorkflow(
    var store: SortationTierStore,
    var validator: SortationTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("sortation tier $code not found")
        validator.requireValid(item)
        item.moveTo(SortationTierStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationTierStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("sortation tier $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newSortationTierWorkflow(store: SortationTierStore, clock: Clock, flags: Registry): SortationTierWorkflow {
    return SortationTierWorkflow(
        store = store,
        validator = defaultSortationTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationTierEngine(flags),
    )
}
