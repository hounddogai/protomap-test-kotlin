package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves declaration scores through their life cycle and keeps a journal of what it did. */
data class DeclarationScoreCoordinator(
    var store: DeclarationScoreStore,
    var validator: DeclarationScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration score $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationScoreStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationScoreStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newDeclarationScoreCoordinator(store: DeclarationScoreStore, clock: Clock): DeclarationScoreCoordinator {
    return DeclarationScoreCoordinator(
        store = store,
        validator = defaultDeclarationScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
