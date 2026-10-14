package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.lastmile.clock.Clock

/** Moves regional redelivery claims through their life cycle and keeps a journal of what it did. */
data class RegionalRedeliveryClaimService(
    var store: RegionalRedeliveryClaimStore,
    var validator: RegionalRedeliveryClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalRedeliveryClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional redelivery claim $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalRedeliveryClaimStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("regional redelivery claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalRedeliveryClaimStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("regional redelivery claim $code not found")
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

fun newRegionalRedeliveryClaimService(store: RegionalRedeliveryClaimStore, clock: Clock): RegionalRedeliveryClaimService {
    return RegionalRedeliveryClaimService(
        store = store,
        validator = defaultRegionalRedeliveryClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalRedeliveryClaimEngine(),
    )
}
