package com.acme.transport.customs.declaration.review

import com.acme.transport.customs.clock.Clock

/** Moves declaration reviews through their life cycle and keeps a journal of what it did. */
data class DeclarationReviewManager(
    var store: DeclarationReviewStore,
    var validator: DeclarationReviewValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("declaration review $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationReviewStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration review $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationReviewStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newDeclarationReviewManager(store: DeclarationReviewStore, clock: Clock): DeclarationReviewManager {
    return DeclarationReviewManager(
        store = store,
        validator = defaultDeclarationReviewValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
