package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.clock.Clock

/** Moves swap quotas through their life cycle and keeps a journal of what it did. */
data class SwapQuotaWorkflow(
    var store: SwapQuotaStore,
    var validator: SwapQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SwapQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("swap quota $code not found")
        validator.requireValid(item)
        item.moveTo(SwapQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("swap quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SwapQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("swap quota $code not found")
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

fun newSwapQuotaWorkflow(store: SwapQuotaStore, clock: Clock): SwapQuotaWorkflow {
    return SwapQuotaWorkflow(
        store = store,
        validator = defaultSwapQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSwapQuotaEngine(),
    )
}
