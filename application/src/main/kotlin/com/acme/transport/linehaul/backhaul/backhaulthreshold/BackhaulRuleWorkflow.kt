package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul rules through their life cycle and keeps a journal of what it did. */
data class BackhaulRuleDesk(
    var store: BackhaulRuleStore,
    var validator: BackhaulRuleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulRuleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("backhaul rule $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulRuleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul rule $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulRuleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("backhaul rule $code not found")
        val result = engine.run(item)
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

fun newBackhaulRuleDesk(store: BackhaulRuleStore, clock: Clock): BackhaulRuleDesk {
    return BackhaulRuleDesk(
        store = store,
        validator = defaultBackhaulRuleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulRuleEngine(),
    )
}
