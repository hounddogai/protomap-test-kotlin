package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual dispute bundles through their life cycle and keeps a journal of what it did. */
data class ManualDisputeBundleDesk(
    var store: ManualDisputeBundleStore,
    var validator: ManualDisputeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualDisputeBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual dispute bundle $code not found")
        validator.requireValid(item)
        item.moveTo(ManualDisputeBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("manual dispute bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualDisputeBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("manual dispute bundle $code not found")
        val result = engine.price(item)
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

fun newManualDisputeBundleDesk(store: ManualDisputeBundleStore, clock: Clock, flags: Registry): ManualDisputeBundleDesk {
    return ManualDisputeBundleDesk(
        store = store,
        validator = defaultManualDisputeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualDisputeBundleEngine(flags),
    )
}
