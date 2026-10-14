package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/**
 * Moves rolling proof of delivery drafts through their life cycle and keeps a journal of what it did.
 */
data class RollingProofOfDeliveryDraftDesk(
    var store: RollingProofOfDeliveryDraftStore,
    var validator: RollingProofOfDeliveryDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingProofOfDeliveryDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling proof of delivery draft $code not found")
        validator.requireValid(item)
        item.moveTo(RollingProofOfDeliveryDraftStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling proof of delivery draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingProofOfDeliveryDraftStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling proof of delivery draft $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newRollingProofOfDeliveryDraftDesk(store: RollingProofOfDeliveryDraftStore, clock: Clock, flags: Registry): RollingProofOfDeliveryDraftDesk {
    return RollingProofOfDeliveryDraftDesk(
        store = store,
        validator = defaultRollingProofOfDeliveryDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingProofOfDeliveryDraftEngine(flags),
    )
}
