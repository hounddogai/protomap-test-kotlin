package com.acme.transport.customs.declaration.agreement

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves capped declaration agreements through their life cycle and keeps a journal of what it did. */
data class CappedDeclarationAgreementManager(
    var store: CappedDeclarationAgreementStore,
    var validator: CappedDeclarationAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedDeclarationAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped declaration agreement $code not found")
        validator.requireValid(item)
        item.moveTo(CappedDeclarationAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped declaration agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedDeclarationAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped declaration agreement $code not found")
        val result = engine.compute(item)
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

fun newCappedDeclarationAgreementManager(store: CappedDeclarationAgreementStore, clock: Clock, flags: Registry): CappedDeclarationAgreementManager {
    return CappedDeclarationAgreementManager(
        store = store,
        validator = defaultCappedDeclarationAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedDeclarationAgreementEngine(flags),
    )
}
