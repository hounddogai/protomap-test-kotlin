package com.acme.transport.customs.hold.quota

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves hold quotas through their life cycle and keeps a journal of what it did. */
data class HoldQuotaService(
    var store: HoldQuotaStore,
    var validator: HoldQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: HoldQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("hold quota $code not found")
        validator.requireValid(item)
        item.moveTo(HoldQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("hold quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(HoldQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("hold quota $code not found")
        val result = engine.compute(item)
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

fun newHoldQuotaService(store: HoldQuotaStore, clock: Clock, flags: Registry): HoldQuotaService {
    return HoldQuotaService(
        store = store,
        validator = defaultHoldQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultHoldQuotaEngine(flags),
    )
}
