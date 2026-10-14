package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.safety.clock.Clock

/** Moves adaptive near miss claims through their life cycle and keeps a journal of what it did. */
data class AdaptiveNearMissClaimManager(
    var store: AdaptiveNearMissClaimStore,
    var validator: AdaptiveNearMissClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveNearMissClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("adaptive near miss claim $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveNearMissClaimStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive near miss claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveNearMissClaimStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("adaptive near miss claim $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newAdaptiveNearMissClaimManager(store: AdaptiveNearMissClaimStore, clock: Clock): AdaptiveNearMissClaimManager {
    return AdaptiveNearMissClaimManager(
        store = store,
        validator = defaultAdaptiveNearMissClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveNearMissClaimEngine(),
    )
}
