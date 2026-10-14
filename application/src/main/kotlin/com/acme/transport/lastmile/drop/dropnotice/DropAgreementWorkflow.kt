package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves drop agreements through their life cycle and keeps a journal of what it did. */
data class DropAgreementWorkflow(
    var store: DropAgreementStore,
    var validator: DropAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("drop agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DropAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("drop agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("drop agreement $code not found")
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

fun newDropAgreementWorkflow(store: DropAgreementStore, clock: Clock, flags: Registry): DropAgreementWorkflow {
    return DropAgreementWorkflow(
        store = store,
        validator = defaultDropAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropAgreementEngine(flags),
    )
}
