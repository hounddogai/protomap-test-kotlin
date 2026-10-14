package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves strict fuel surcharge summaries through their life cycle and keeps a journal of what it did.
 */
data class StrictFuelSurchargeSummaryWorkflow(
    var store: StrictFuelSurchargeSummaryStore,
    var validator: StrictFuelSurchargeSummaryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictFuelSurchargeSummaryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict fuel surcharge summary $code not found")
        validator.requireValid(item)
        item.moveTo(StrictFuelSurchargeSummaryStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("strict fuel surcharge summary $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictFuelSurchargeSummaryStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict fuel surcharge summary $code not found")
        val result = engine.compute(item)
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

fun newStrictFuelSurchargeSummaryWorkflow(store: StrictFuelSurchargeSummaryStore, clock: Clock): StrictFuelSurchargeSummaryWorkflow {
    return StrictFuelSurchargeSummaryWorkflow(
        store = store,
        validator = defaultStrictFuelSurchargeSummaryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictFuelSurchargeSummaryEngine(),
    )
}
