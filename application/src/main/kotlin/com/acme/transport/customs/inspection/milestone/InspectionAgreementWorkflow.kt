package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.clock.Clock

/** Moves inspection agreements through their life cycle and keeps a journal of what it did. */
data class InspectionAgreementWorkflow(
    var store: InspectionAgreementStore,
    var validator: InspectionAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: InspectionAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("inspection agreement $code not found")
        validator.requireValid(item)
        item.moveTo(InspectionAgreementStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("inspection agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(InspectionAgreementStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("inspection agreement $code not found")
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
            if (item.startsAt > cutoff) {
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

fun newInspectionAgreementWorkflow(store: InspectionAgreementStore, clock: Clock): InspectionAgreementWorkflow {
    return InspectionAgreementWorkflow(
        store = store,
        validator = defaultInspectionAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultInspectionAgreementEngine(),
    )
}
