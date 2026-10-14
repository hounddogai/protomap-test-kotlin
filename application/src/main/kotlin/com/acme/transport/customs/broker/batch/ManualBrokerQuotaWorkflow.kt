package com.acme.transport.customs.broker.batch

import com.acme.transport.customs.clock.Clock

/** Moves manual broker quotas through their life cycle and keeps a journal of what it did. */
data class ManualBrokerQuotaService(
    var store: ManualBrokerQuotaStore,
    var validator: ManualBrokerQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualBrokerQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual broker quota $code not found")
        validator.requireValid(item)
        item.moveTo(ManualBrokerQuotaStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual broker quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualBrokerQuotaStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual broker quota $code not found")
        val result = engine.run(item)
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

fun newManualBrokerQuotaService(store: ManualBrokerQuotaStore, clock: Clock): ManualBrokerQuotaService {
    return ManualBrokerQuotaService(
        store = store,
        validator = defaultManualBrokerQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualBrokerQuotaEngine(),
    )
}
