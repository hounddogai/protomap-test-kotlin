package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.terminal.clock.Clock

/** Moves global sortation drafts through their life cycle and keeps a journal of what it did. */
data class GlobalSortationDraftCoordinator(
    var store: GlobalSortationDraftStore,
    var validator: GlobalSortationDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalSortationDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global sortation draft $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalSortationDraftStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("global sortation draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalSortationDraftStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global sortation draft $code not found")
        val result = engine.run(item)
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

fun newGlobalSortationDraftCoordinator(store: GlobalSortationDraftStore, clock: Clock): GlobalSortationDraftCoordinator {
    return GlobalSortationDraftCoordinator(
        store = store,
        validator = defaultGlobalSortationDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalSortationDraftEngine(),
    )
}
