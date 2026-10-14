package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.terminal.clock.Clock

/** Moves dwell agreements through their life cycle and keeps a journal of what it did. */
data class DwellAgreementService(
    var store: DwellAgreementStore,
    var validator: DwellAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DwellAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("dwell agreement $code not found")
        validator.requireValid(item)
        item.moveTo(DwellAgreementStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellAgreementStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("dwell agreement $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newDwellAgreementService(store: DwellAgreementStore, clock: Clock): DwellAgreementService {
    return DwellAgreementService(
        store = store,
        validator = defaultDwellAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDwellAgreementEngine(),
    )
}
