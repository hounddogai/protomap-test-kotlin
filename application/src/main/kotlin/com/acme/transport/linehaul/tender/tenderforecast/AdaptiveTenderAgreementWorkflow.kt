package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves adaptive tender agreements through their life cycle and keeps a journal of what it did. */
data class AdaptiveTenderAgreementCoordinator(
    var store: AdaptiveTenderAgreementStore,
    var validator: AdaptiveTenderAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveTenderAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive tender agreement $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTenderAgreementStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive tender agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTenderAgreementStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("adaptive tender agreement $code not found")
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

fun newAdaptiveTenderAgreementCoordinator(store: AdaptiveTenderAgreementStore, clock: Clock, flags: Registry): AdaptiveTenderAgreementCoordinator {
    return AdaptiveTenderAgreementCoordinator(
        store = store,
        validator = defaultAdaptiveTenderAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveTenderAgreementEngine(flags),
    )
}
