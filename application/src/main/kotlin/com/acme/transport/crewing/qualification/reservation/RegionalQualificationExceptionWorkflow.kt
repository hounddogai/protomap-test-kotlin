package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.crewing.clock.Clock

/**
 * Moves regional qualification exceptions through their life cycle and keeps a journal of what it did.
 */
data class RegionalQualificationExceptionWorkflow(
    var store: RegionalQualificationExceptionStore,
    var validator: RegionalQualificationExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalQualificationExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional qualification exception $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalQualificationExceptionStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional qualification exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalQualificationExceptionStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional qualification exception $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRegionalQualificationExceptionWorkflow(store: RegionalQualificationExceptionStore, clock: Clock): RegionalQualificationExceptionWorkflow {
    return RegionalQualificationExceptionWorkflow(
        store = store,
        validator = defaultRegionalQualificationExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalQualificationExceptionEngine(),
    )
}
