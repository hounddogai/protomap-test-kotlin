package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves dispute manifests through their life cycle and keeps a journal of what it did. */
data class DisputeManifestService(
    var store: DisputeManifestStore,
    var validator: DisputeManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DisputeManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dispute manifest $code not found")
        validator.requireValid(item)
        item.moveTo(DisputeManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("dispute manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DisputeManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("dispute manifest $code not found")
        val result = engine.evaluate(item)
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

fun newDisputeManifestService(store: DisputeManifestStore, clock: Clock, flags: Registry): DisputeManifestService {
    return DisputeManifestService(
        store = store,
        validator = defaultDisputeManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDisputeManifestEngine(flags),
    )
}
