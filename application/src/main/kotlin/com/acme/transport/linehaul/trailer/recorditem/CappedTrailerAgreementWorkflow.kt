package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capped trailer agreements through their life cycle and keeps a journal of what it did. */
data class CappedTrailerAgreementManager(
    var store: CappedTrailerAgreementStore,
    var validator: CappedTrailerAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTrailerAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped trailer agreement $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerAgreementStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerAgreementStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped trailer agreement $code not found")
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

fun newCappedTrailerAgreementManager(store: CappedTrailerAgreementStore, clock: Clock, flags: Registry): CappedTrailerAgreementManager {
    return CappedTrailerAgreementManager(
        store = store,
        validator = defaultCappedTrailerAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTrailerAgreementEngine(flags),
    )
}
