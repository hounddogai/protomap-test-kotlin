package com.acme.transport.linehaul.tender.profile

import com.acme.transport.linehaul.clock.Clock

/** Moves tender quotas through their life cycle and keeps a journal of what it did. */
data class TenderQuotaManager(
    var store: TenderQuotaStore,
    var validator: TenderQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tender quota $code not found")
        validator.requireValid(item)
        item.moveTo(TenderQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tender quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("tender quota $code not found")
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

fun newTenderQuotaManager(store: TenderQuotaStore, clock: Clock): TenderQuotaManager {
    return TenderQuotaManager(
        store = store,
        validator = defaultTenderQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderQuotaEngine(),
    )
}
