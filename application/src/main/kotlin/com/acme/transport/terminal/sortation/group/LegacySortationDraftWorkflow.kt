package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves legacy sortation drafts through their life cycle and keeps a journal of what it did. */
data class LegacySortationDraftService(
    var store: LegacySortationDraftStore,
    var validator: LegacySortationDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacySortationDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy sortation draft $code not found")
        validator.requireValid(item)
        item.moveTo(LegacySortationDraftStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy sortation draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacySortationDraftStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("legacy sortation draft $code not found")
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

fun newLegacySortationDraftService(store: LegacySortationDraftStore, clock: Clock, flags: Registry): LegacySortationDraftService {
    return LegacySortationDraftService(
        store = store,
        validator = defaultLegacySortationDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacySortationDraftEngine(flags),
    )
}
