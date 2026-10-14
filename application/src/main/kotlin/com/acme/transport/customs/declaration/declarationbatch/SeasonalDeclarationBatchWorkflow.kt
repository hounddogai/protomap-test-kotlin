package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves seasonal declaration batches through their life cycle and keeps a journal of what it did. */
data class SeasonalDeclarationBatchDesk(
    var store: SeasonalDeclarationBatchStore,
    var validator: SeasonalDeclarationBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SeasonalDeclarationBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal declaration batch $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalDeclarationBatchStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal declaration batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalDeclarationBatchStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("seasonal declaration batch $code not found")
        val result = engine.run(item)
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

fun newSeasonalDeclarationBatchDesk(store: SeasonalDeclarationBatchStore, clock: Clock, flags: Registry): SeasonalDeclarationBatchDesk {
    return SeasonalDeclarationBatchDesk(
        store = store,
        validator = defaultSeasonalDeclarationBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSeasonalDeclarationBatchEngine(flags),
    )
}
