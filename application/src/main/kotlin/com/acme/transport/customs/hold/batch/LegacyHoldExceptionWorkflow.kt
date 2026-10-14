package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves legacy hold exceptions through their life cycle and keeps a journal of what it did. */
data class LegacyHoldExceptionManager(
    var store: LegacyHoldExceptionStore,
    var validator: LegacyHoldExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyHoldExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy hold exception $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyHoldExceptionStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy hold exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyHoldExceptionStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy hold exception $code not found")
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

fun newLegacyHoldExceptionManager(store: LegacyHoldExceptionStore, clock: Clock, flags: Registry): LegacyHoldExceptionManager {
    return LegacyHoldExceptionManager(
        store = store,
        validator = defaultLegacyHoldExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyHoldExceptionEngine(flags),
    )
}
