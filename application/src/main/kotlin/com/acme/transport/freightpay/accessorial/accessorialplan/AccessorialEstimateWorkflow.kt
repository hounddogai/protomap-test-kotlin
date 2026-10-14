package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial estimates through their life cycle and keeps a journal of what it did. */
data class AccessorialEstimateWorkflow(
    var store: AccessorialEstimateStore,
    var validator: AccessorialEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialEstimateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("accessorial estimate $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialEstimateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialEstimateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("accessorial estimate $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newAccessorialEstimateWorkflow(store: AccessorialEstimateStore, clock: Clock): AccessorialEstimateWorkflow {
    return AccessorialEstimateWorkflow(
        store = store,
        validator = defaultAccessorialEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialEstimateEngine(),
    )
}
