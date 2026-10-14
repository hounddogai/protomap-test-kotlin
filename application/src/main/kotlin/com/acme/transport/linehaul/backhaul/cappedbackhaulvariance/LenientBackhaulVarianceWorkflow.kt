package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.linehaul.clock.Clock

/** Moves lenient backhaul variances through their life cycle and keeps a journal of what it did. */
data class LenientBackhaulVarianceWorkflow(
    var store: LenientBackhaulVarianceStore,
    var validator: LenientBackhaulVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientBackhaulVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient backhaul variance $code not found")
        validator.requireValid(item)
        item.moveTo(LenientBackhaulVarianceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient backhaul variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientBackhaulVarianceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient backhaul variance $code not found")
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

fun newLenientBackhaulVarianceWorkflow(store: LenientBackhaulVarianceStore, clock: Clock): LenientBackhaulVarianceWorkflow {
    return LenientBackhaulVarianceWorkflow(
        store = store,
        validator = defaultLenientBackhaulVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientBackhaulVarianceEngine(),
    )
}
