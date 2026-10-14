package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.network.clock.Clock

/** Moves adaptive zone agreements through their life cycle and keeps a journal of what it did. */
data class AdaptiveZoneAgreementCoordinator(
    var store: AdaptiveZoneAgreementStore,
    var validator: AdaptiveZoneAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveZoneAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive zone agreement $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveZoneAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive zone agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveZoneAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive zone agreement $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newAdaptiveZoneAgreementCoordinator(store: AdaptiveZoneAgreementStore, clock: Clock): AdaptiveZoneAgreementCoordinator {
    return AdaptiveZoneAgreementCoordinator(
        store = store,
        validator = defaultAdaptiveZoneAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveZoneAgreementEngine(),
    )
}
