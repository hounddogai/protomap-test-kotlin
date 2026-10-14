package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration overrides through their life cycle and keeps a journal of what it did. */
data class DeclarationOverrideDesk(
    var store: DeclarationOverrideStore,
    var validator: DeclarationOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("declaration override $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationOverrideStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationOverrideStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("declaration override $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newDeclarationOverrideDesk(store: DeclarationOverrideStore, clock: Clock, flags: Registry): DeclarationOverrideDesk {
    return DeclarationOverrideDesk(
        store = store,
        validator = defaultDeclarationOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationOverrideEngine(flags),
    )
}
