package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary declaration digests through their life cycle and keeps a journal of what it did. */
data class PrimaryDeclarationDigestCoordinator(
    var store: PrimaryDeclarationDigestStore,
    var validator: PrimaryDeclarationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryDeclarationDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary declaration digest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryDeclarationDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("primary declaration digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryDeclarationDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary declaration digest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newPrimaryDeclarationDigestCoordinator(store: PrimaryDeclarationDigestStore, clock: Clock, flags: Registry): PrimaryDeclarationDigestCoordinator {
    return PrimaryDeclarationDigestCoordinator(
        store = store,
        validator = defaultPrimaryDeclarationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryDeclarationDigestEngine(flags),
    )
}
