package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.clock.Clock

/** Moves default roster agreements through their life cycle and keeps a journal of what it did. */
data class DefaultRosterAgreementCoordinator(
    var store: DefaultRosterAgreementStore,
    var validator: DefaultRosterAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultRosterAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("default roster agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultRosterAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("default roster agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultRosterAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("default roster agreement $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newDefaultRosterAgreementCoordinator(store: DefaultRosterAgreementStore, clock: Clock): DefaultRosterAgreementCoordinator {
    return DefaultRosterAgreementCoordinator(
        store = store,
        validator = defaultDefaultRosterAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultRosterAgreementEngine(),
    )
}
