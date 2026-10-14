package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual rate card milestones through their life cycle and keeps a journal of what it did. */
data class ManualRateCardMilestoneManager(
    var store: ManualRateCardMilestoneStore,
    var validator: ManualRateCardMilestoneValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualRateCardMilestoneEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual rate card milestone $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRateCardMilestoneStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual rate card milestone $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRateCardMilestoneStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual rate card milestone $code not found")
        val result = engine.evaluate(item)
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

fun newManualRateCardMilestoneManager(store: ManualRateCardMilestoneStore, clock: Clock, flags: Registry): ManualRateCardMilestoneManager {
    return ManualRateCardMilestoneManager(
        store = store,
        validator = defaultManualRateCardMilestoneValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualRateCardMilestoneEngine(flags),
    )
}
