package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy dispute tiers through their life cycle and keeps a journal of what it did. */
data class LegacyDisputeTierManager(
    var store: LegacyDisputeTierStore,
    var validator: LegacyDisputeTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyDisputeTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("legacy dispute tier $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyDisputeTierStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy dispute tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyDisputeTierStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("legacy dispute tier $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newLegacyDisputeTierManager(store: LegacyDisputeTierStore, clock: Clock): LegacyDisputeTierManager {
    return LegacyDisputeTierManager(
        store = store,
        validator = defaultLegacyDisputeTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyDisputeTierEngine(),
    )
}
