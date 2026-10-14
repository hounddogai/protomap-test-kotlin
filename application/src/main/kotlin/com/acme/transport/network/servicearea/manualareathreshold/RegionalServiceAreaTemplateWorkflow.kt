package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.network.clock.Clock

/**
 * Moves regional service area templates through their life cycle and keeps a journal of what it did.
 */
data class RegionalServiceAreaTemplateWorkflow(
    var store: RegionalServiceAreaTemplateStore,
    var validator: RegionalServiceAreaTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional service area template $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalServiceAreaTemplateStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional service area template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalServiceAreaTemplateStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newRegionalServiceAreaTemplateWorkflow(store: RegionalServiceAreaTemplateStore, clock: Clock): RegionalServiceAreaTemplateWorkflow {
    return RegionalServiceAreaTemplateWorkflow(
        store = store,
        validator = defaultRegionalServiceAreaTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
