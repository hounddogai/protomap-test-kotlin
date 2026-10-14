package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy accessorial tiers through their life cycle and keeps a journal of what it did. */
data class LegacyAccessorialTierService(
    var store: LegacyAccessorialTierStore,
    var validator: LegacyAccessorialTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyAccessorialTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy accessorial tier $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyAccessorialTierStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy accessorial tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyAccessorialTierStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("legacy accessorial tier $code not found")
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

fun newLegacyAccessorialTierService(store: LegacyAccessorialTierStore, clock: Clock): LegacyAccessorialTierService {
    return LegacyAccessorialTierService(
        store = store,
        validator = defaultLegacyAccessorialTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyAccessorialTierEngine(),
    )
}
