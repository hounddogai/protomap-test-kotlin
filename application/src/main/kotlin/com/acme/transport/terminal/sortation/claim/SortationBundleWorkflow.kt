package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation bundles through their life cycle and keeps a journal of what it did. */
data class SortationBundleWorkflow(
    var store: SortationBundleStore,
    var validator: SortationBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("sortation bundle $code not found")
        validator.requireValid(item)
        item.moveTo(SortationBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("sortation bundle $code not found")
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

fun newSortationBundleWorkflow(store: SortationBundleStore, clock: Clock, flags: Registry): SortationBundleWorkflow {
    return SortationBundleWorkflow(
        store = store,
        validator = defaultSortationBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationBundleEngine(flags),
    )
}
