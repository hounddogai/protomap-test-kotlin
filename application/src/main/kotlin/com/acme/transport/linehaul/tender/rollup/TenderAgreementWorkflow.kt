package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tender agreements through their life cycle and keeps a journal of what it did. */
data class TenderAgreementManager(
    var store: TenderAgreementStore,
    var validator: TenderAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tender agreement $code not found")
        validator.requireValid(item)
        item.moveTo(TenderAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tender agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("tender agreement $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newTenderAgreementManager(store: TenderAgreementStore, clock: Clock, flags: Registry): TenderAgreementManager {
    return TenderAgreementManager(
        store = store,
        validator = defaultTenderAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderAgreementEngine(flags),
    )
}
