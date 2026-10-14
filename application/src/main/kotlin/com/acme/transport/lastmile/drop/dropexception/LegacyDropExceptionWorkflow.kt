package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves legacy drop exceptions through their life cycle and keeps a journal of what it did. */
data class LegacyDropExceptionService(
    var store: LegacyDropExceptionStore,
    var validator: LegacyDropExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyDropExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy drop exception $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDropExceptionStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy drop exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDropExceptionStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy drop exception $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLegacyDropExceptionService(store: LegacyDropExceptionStore, clock: Clock, flags: Registry): LegacyDropExceptionService {
    return LegacyDropExceptionService(
        store = store,
        validator = defaultLegacyDropExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyDropExceptionEngine(flags),
    )
}
