package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.clock.Clock

/** Moves rolling swap drafts through their life cycle and keeps a journal of what it did. */
data class RollingSwapDraftManager(
    var store: RollingSwapDraftStore,
    var validator: RollingSwapDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingSwapDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling swap draft $code not found")
        validator.requireValid(item)
        item.moveTo(RollingSwapDraftStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling swap draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingSwapDraftStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling swap draft $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRollingSwapDraftManager(store: RollingSwapDraftStore, clock: Clock): RollingSwapDraftManager {
    return RollingSwapDraftManager(
        store = store,
        validator = defaultRollingSwapDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingSwapDraftEngine(),
    )
}
