package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.Clock

/** Moves qualification claims through their life cycle and keeps a journal of what it did. */
data class QualificationClaimManager(
    var store: QualificationClaimStore,
    var validator: QualificationClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: QualificationClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("qualification claim $code not found")
        validator.requireValid(item)
        item.moveTo(QualificationClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("qualification claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(QualificationClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("qualification claim $code not found")
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

fun newQualificationClaimManager(store: QualificationClaimStore, clock: Clock): QualificationClaimManager {
    return QualificationClaimManager(
        store = store,
        validator = defaultQualificationClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultQualificationClaimEngine(),
    )
}
