package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.clock.Clock

/** Moves declaration estimates through their life cycle and keeps a journal of what it did. */
data class DeclarationEstimateCoordinator(
    var store: DeclarationEstimateStore,
    var validator: DeclarationEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("declaration estimate $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationEstimateStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationEstimateStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDeclarationEstimateCoordinator(store: DeclarationEstimateStore, clock: Clock): DeclarationEstimateCoordinator {
    return DeclarationEstimateCoordinator(
        store = store,
        validator = defaultDeclarationEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
