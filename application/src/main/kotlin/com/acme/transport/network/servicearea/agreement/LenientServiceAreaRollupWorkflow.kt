package com.acme.transport.network.servicearea.agreement

import com.acme.transport.network.clock.Clock

/** Moves lenient service area rollups through their life cycle and keeps a journal of what it did. */
data class LenientServiceAreaRollupWorkflow(
    var store: LenientServiceAreaRollupStore,
    var validator: LenientServiceAreaRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientServiceAreaRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient service area rollup $code not found")
        validator.requireValid(item)
        item.moveTo(LenientServiceAreaRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient service area rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientServiceAreaRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient service area rollup $code not found")
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

fun newLenientServiceAreaRollupWorkflow(store: LenientServiceAreaRollupStore, clock: Clock): LenientServiceAreaRollupWorkflow {
    return LenientServiceAreaRollupWorkflow(
        store = store,
        validator = defaultLenientServiceAreaRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientServiceAreaRollupEngine(),
    )
}
