package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.linehaul.clock.Clock

/** Moves regional capacity agreements through their life cycle and keeps a journal of what it did. */
data class RegionalCapacityAgreementManager(
    var store: RegionalCapacityAgreementStore,
    var validator: RegionalCapacityAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalCapacityAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("regional capacity agreement $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalCapacityAgreementStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional capacity agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalCapacityAgreementStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional capacity agreement $code not found")
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

fun newRegionalCapacityAgreementManager(store: RegionalCapacityAgreementStore, clock: Clock): RegionalCapacityAgreementManager {
    return RegionalCapacityAgreementManager(
        store = store,
        validator = defaultRegionalCapacityAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalCapacityAgreementEngine(),
    )
}
