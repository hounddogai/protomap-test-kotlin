package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves capped tender claims through their life cycle and keeps a journal of what it did. */
data class CappedTenderClaimService(
    var store: CappedTenderClaimStore,
    var validator: CappedTenderClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTenderClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("capped tender claim $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTenderClaimStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped tender claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTenderClaimStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped tender claim $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newCappedTenderClaimService(store: CappedTenderClaimStore, clock: Clock, flags: Registry): CappedTenderClaimService {
    return CappedTenderClaimService(
        store = store,
        validator = defaultCappedTenderClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTenderClaimEngine(flags),
    )
}
