package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.clock.Clock

/** Moves declaration exceptions through their life cycle and keeps a journal of what it did. */
data class DeclarationExceptionCoordinator(
    var store: DeclarationExceptionStore,
    var validator: DeclarationExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("declaration exception $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("declaration exception $code not found")
        val result = engine.price(item)
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

fun newDeclarationExceptionCoordinator(store: DeclarationExceptionStore, clock: Clock): DeclarationExceptionCoordinator {
    return DeclarationExceptionCoordinator(
        store = store,
        validator = defaultDeclarationExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationExceptionEngine(),
    )
}
