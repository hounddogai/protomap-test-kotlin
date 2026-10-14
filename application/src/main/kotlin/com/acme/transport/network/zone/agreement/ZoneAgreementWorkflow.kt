package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves zone agreements through their life cycle and keeps a journal of what it did. */
data class ZoneAgreementCoordinator(
    var store: ZoneAgreementStore,
    var validator: ZoneAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ZoneAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("zone agreement $code not found")
        validator.requireValid(item)
        item.moveTo(ZoneAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("zone agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ZoneAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("zone agreement $code not found")
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

fun newZoneAgreementCoordinator(store: ZoneAgreementStore, clock: Clock, flags: Registry): ZoneAgreementCoordinator {
    return ZoneAgreementCoordinator(
        store = store,
        validator = defaultZoneAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultZoneAgreementEngine(flags),
    )
}
