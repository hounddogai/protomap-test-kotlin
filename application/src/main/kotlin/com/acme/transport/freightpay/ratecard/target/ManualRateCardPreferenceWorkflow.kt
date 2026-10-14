package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.clock.Clock

/** Moves manual rate card preferences through their life cycle and keeps a journal of what it did. */
data class ManualRateCardPreferenceCoordinator(
    var store: ManualRateCardPreferenceStore,
    var validator: ManualRateCardPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualRateCardPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual rate card preference $code not found")
        validator.requireValid(item)
        item.moveTo(ManualRateCardPreferenceStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("manual rate card preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualRateCardPreferenceStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("manual rate card preference $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newManualRateCardPreferenceCoordinator(store: ManualRateCardPreferenceStore, clock: Clock): ManualRateCardPreferenceCoordinator {
    return ManualRateCardPreferenceCoordinator(
        store = store,
        validator = defaultManualRateCardPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualRateCardPreferenceEngine(),
    )
}
