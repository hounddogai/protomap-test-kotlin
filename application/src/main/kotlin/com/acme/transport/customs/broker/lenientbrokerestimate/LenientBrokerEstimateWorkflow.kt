package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.clock.Clock

/** Moves lenient broker estimates through their life cycle and keeps a journal of what it did. */
data class LenientBrokerEstimateWorkflow(
    var store: LenientBrokerEstimateStore,
    var validator: LenientBrokerEstimateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient broker estimate $code not found")
        validator.requireValid(item)
        item.moveTo(LenientBrokerEstimateStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient broker estimate $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientBrokerEstimateStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newLenientBrokerEstimateWorkflow(store: LenientBrokerEstimateStore, clock: Clock): LenientBrokerEstimateWorkflow {
    return LenientBrokerEstimateWorkflow(
        store = store,
        validator = defaultLenientBrokerEstimateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
