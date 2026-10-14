package com.acme.transport.customs.declaration.baseline

import com.acme.transport.customs.clock.Clock

/** Moves declaration baselines through their life cycle and keeps a journal of what it did. */
data class DeclarationBaselineCoordinator(
    var store: DeclarationBaselineStore,
    var validator: DeclarationBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration baseline $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationBaselineStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationBaselineStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDeclarationBaselineCoordinator(store: DeclarationBaselineStore, clock: Clock): DeclarationBaselineCoordinator {
    return DeclarationBaselineCoordinator(
        store = store,
        validator = defaultDeclarationBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
