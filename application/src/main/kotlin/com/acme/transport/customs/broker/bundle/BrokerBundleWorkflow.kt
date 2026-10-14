package com.acme.transport.customs.broker.bundle

import com.acme.transport.customs.clock.Clock

/** Moves broker bundles through their life cycle and keeps a journal of what it did. */
data class BrokerBundleManager(
    var store: BrokerBundleStore,
    var validator: BrokerBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BrokerBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("broker bundle $code not found")
        validator.requireValid(item)
        item.moveTo(BrokerBundleStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("broker bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BrokerBundleStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("broker bundle $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newBrokerBundleManager(store: BrokerBundleStore, clock: Clock): BrokerBundleManager {
    return BrokerBundleManager(
        store = store,
        validator = defaultBrokerBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBrokerBundleEngine(),
    )
}
