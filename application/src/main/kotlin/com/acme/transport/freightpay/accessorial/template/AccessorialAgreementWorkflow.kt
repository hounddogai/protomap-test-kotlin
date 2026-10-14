package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial agreements through their life cycle and keeps a journal of what it did. */
data class AccessorialAgreementService(
    var store: AccessorialAgreementStore,
    var validator: AccessorialAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("accessorial agreement $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialAgreementStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialAgreementStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("accessorial agreement $code not found")
        val result = engine.evaluate(item)
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

fun newAccessorialAgreementService(store: AccessorialAgreementStore, clock: Clock): AccessorialAgreementService {
    return AccessorialAgreementService(
        store = store,
        validator = defaultAccessorialAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialAgreementEngine(),
    )
}
