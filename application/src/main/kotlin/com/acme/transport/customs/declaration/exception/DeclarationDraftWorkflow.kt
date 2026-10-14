package com.acme.transport.customs.declaration.exception

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves declaration drafts through their life cycle and keeps a journal of what it did. */
data class DeclarationDraftCoordinator(
    var store: DeclarationDraftStore,
    var validator: DeclarationDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DeclarationDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("declaration draft $code not found")
        validator.requireValid(item)
        item.moveTo(DeclarationDraftStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("declaration draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DeclarationDraftStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("declaration draft $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newDeclarationDraftCoordinator(store: DeclarationDraftStore, clock: Clock, flags: Registry): DeclarationDraftCoordinator {
    return DeclarationDraftCoordinator(
        store = store,
        validator = defaultDeclarationDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDeclarationDraftEngine(flags),
    )
}
