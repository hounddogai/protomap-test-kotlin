package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves adaptive audit rules through their life cycle and keeps a journal of what it did. */
data class AdaptiveAuditRuleManager(
    var store: AdaptiveAuditRuleStore,
    var validator: AdaptiveAuditRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AdaptiveAuditRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("adaptive audit rule $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveAuditRuleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive audit rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveAuditRuleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("adaptive audit rule $code not found")
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

fun newAdaptiveAuditRuleManager(store: AdaptiveAuditRuleStore, clock: Clock, flags: Registry): AdaptiveAuditRuleManager {
    return AdaptiveAuditRuleManager(
        store = store,
        validator = defaultAdaptiveAuditRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAdaptiveAuditRuleEngine(flags),
    )
}
