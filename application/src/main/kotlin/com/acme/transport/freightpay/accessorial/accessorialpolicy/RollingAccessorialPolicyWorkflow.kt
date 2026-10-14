package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling accessorial policies through their life cycle and keeps a journal of what it did. */
data class RollingAccessorialPolicyWorkflow(
    var store: RollingAccessorialPolicyStore,
    var validator: RollingAccessorialPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingAccessorialPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling accessorial policy $code not found")
        validator.requireValid(item)
        item.moveTo(RollingAccessorialPolicyStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling accessorial policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingAccessorialPolicyStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling accessorial policy $code not found")
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

fun newRollingAccessorialPolicyWorkflow(store: RollingAccessorialPolicyStore, clock: Clock, flags: Registry): RollingAccessorialPolicyWorkflow {
    return RollingAccessorialPolicyWorkflow(
        store = store,
        validator = defaultRollingAccessorialPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingAccessorialPolicyEngine(flags),
    )
}
