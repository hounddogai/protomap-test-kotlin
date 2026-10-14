package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.customs.clock.Clock

/** Moves strict declaration baselines through their life cycle and keeps a journal of what it did. */
data class StrictDeclarationBaselineManager(
    var store: StrictDeclarationBaselineStore,
    var validator: StrictDeclarationBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict declaration baseline $code not found")
        validator.requireValid(item)
        item.moveTo(StrictDeclarationBaselineStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("strict declaration baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictDeclarationBaselineStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newStrictDeclarationBaselineManager(store: StrictDeclarationBaselineStore, clock: Clock): StrictDeclarationBaselineManager {
    return StrictDeclarationBaselineManager(
        store = store,
        validator = defaultStrictDeclarationBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
